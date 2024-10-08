package io.github.devatherock.scheduler

import java.time.Duration

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Query

import io.micronaut.context.env.Environment
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import jakarta.inject.Inject
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

/**
 * Integration test for {@link KafkaLagCollector}
 */
abstract class KafkaLagCollectorBaseSpec extends Specification {
    @Inject
    @Client('${test.server.url}')
    HttpClient httpClient

    @Inject
    Environment environment

    KafkaProducer producer
    KafkaConsumer consumer
    String topic = 'test-topic'
    PollingConditions conditions = new PollingConditions(timeout: 15, initialDelay: 1, delay: 1)
    InfluxDB influxDB

    void setup() {
        // Initialize kafka producer and consumer
        def commonConfig = [
                'bootstrap.servers': "${System.env.DOCKER_NETWORK_IP ?: 'localhost'}:9092".toString(),
        ]

        def producerConfig = new HashMap(commonConfig)
        producerConfig['key.serializer'] = 'org.apache.kafka.common.serialization.StringSerializer'
        producerConfig['value.serializer'] = 'org.apache.kafka.common.serialization.StringSerializer'
        producer = new KafkaProducer(producerConfig)

        def consumerConfig = new HashMap(commonConfig)
        consumerConfig['group.id'] = 'test-consumer'
        consumerConfig['key.deserializer'] = 'org.apache.kafka.common.serialization.StringDeserializer'
        consumerConfig['value.deserializer'] = 'org.apache.kafka.common.serialization.StringDeserializer'
        consumerConfig['auto.offset.reset'] = 'earliest'
        consumer = new KafkaConsumer(consumerConfig)

        // Initialize influx client
        influxDB = InfluxDBFactory.connect(
                environment.getProperty('micronaut.metrics.export.influx.uri', String).get(),
        )
        influxDB.database = 'mydb'
    }

    void 'test prometheus metrics'() {
        given: 'consume old messages and first message, then stop consumer'
        consumer.subscribe([topic])
        producer.send(new ProducerRecord(topic, 'key-1', 'message-1'))
        def records = consumer.poll(Duration.ofSeconds(10))
        assert records.size() > 0
        consumer.commitSync()
        consumer.unsubscribe()

        when: 'produce 2 more messages'
        producer.send(new ProducerRecord(topic, 'key-2', 'message-2'))
        producer.send(new ProducerRecord(topic, 'key-3', 'message-3'))

        then:
        conditions.eventually {
            httpClient.toBlocking().retrieve('/prometheus').contains('''
            kafka_consumer_lag_max{cluster_name="test-cluster",group="test-consumer",partition="0",topic="test-topic"} 2.0
        '''.trim()
            )
        }

        and:
        conditions.eventually {
            influxDB.query(new Query(
                    '''select last(upper) from kafka_consumer_lag where "cluster_name" = 'test-cluster' ''' +
                            '''and "group" = 'test-consumer' and "topic" = 'test-topic' and "partition" = '0' '''.trim()
            )).results[0].series[0].values[0][1] == 2
        }
    }
}
