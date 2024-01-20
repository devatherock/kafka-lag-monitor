package io.github.devatherock.scheduler

import java.time.Duration

import javax.inject.Inject

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

/**
 * Integration test for {@link KafkaLagCollector}
 */
abstract class KafkaLagCollectorBaseSpec extends Specification {
    @Inject
    @Client('${test.server.url}')
    HttpClient httpClient

    KafkaProducer producer
    KafkaConsumer consumer
    String topic = 'test-topic'
    PollingConditions conditions = new PollingConditions(timeout : 15, initialDelay : 1, delay : 1)

    void setup() {
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
    }

    void 'test prometheus metrics'() {
        given: 'consume old messages and first message, then stop consumer'
        consumer.subscribe([topic])
        producer.send(new ProducerRecord(topic, 'key-1', 'message-1'))
        def records = consumer.poll(Duration.ofSeconds(10))
        assert records.size() > 0
        consumer.commitSync()
        consumer.unsubscribe()

        and: 'produce 2 more messages'
        producer.send(new ProducerRecord(topic, 'key-2', 'message-2'))
        producer.send(new ProducerRecord(topic, 'key-3', 'message-3'))

        expect:
        conditions.eventually {
            httpClient.toBlocking().retrieve('/prometheus').contains('''
            kafka_consumer_lag_max{cluster_name="test-cluster",group="test-consumer",partition="0",topic="test-topic",} 2.0
        '''.trim()
            )
        }
    }
}
