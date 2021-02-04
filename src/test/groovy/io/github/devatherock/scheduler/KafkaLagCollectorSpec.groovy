package io.github.devatherock.scheduler

import io.github.devatherock.config.ApplicationProperties
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult
import org.apache.kafka.common.KafkaFuture
import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Test class for {@link KafkaLagCollector}
 */
class KafkaLagCollectorSpec extends Specification {
    @Subject
    KafkaLagCollector lagCollector

    AdminClient adminClient = Mock()
    ApplicationProperties config = new ApplicationProperties()
    ScheduledExecutorService scheduler = Mock()
    MeterRegistry meterRegistry = new SimpleMeterRegistry()

    void setup() {
        lagCollector = new KafkaLagCollector(meterRegistry, config, scheduler)
        lagCollector.adminClients['test-cluster'] = adminClient
    }

    void 'test collect consumer group lag - group offsets is empty'() {
        given:
        String clusterName = 'test-cluster'
        String groupId = 'test-consumer'

        and:
        ListConsumerGroupOffsetsResult groupOffsetsResult = Mock()
        KafkaFuture kafkaFuture = Mock()

        when:
        lagCollector.collectConsumerGroupLag(clusterName, groupId)

        then:
        1 * adminClient.listConsumerGroupOffsets(groupId) >> groupOffsetsResult
        1 * groupOffsetsResult.partitionsToOffsetAndMetadata() >> kafkaFuture
        1 * kafkaFuture.get(config.lagMonitor.timeoutSeconds, TimeUnit.SECONDS) >> groupOffsets

        then: 'no metrics will be recorded'
        !meterRegistry.meters.any {it.id.name == 'kafka.partition.offset'}
        !meterRegistry.meters.any {it.id.name == 'kafka.consumer.offset'}
        !meterRegistry.meters.any {it.id.name == 'kafka.consumer.lag'}

        where:
        groupOffsets << [
                null,
                [:]
        ]
    }
}
