package io.github.devatherock.scheduler

import io.github.devatherock.config.ApplicationProperties
import io.github.devatherock.config.ApplicationProperties.LagMonitorConfig
import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.ConsumerGroupListing
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult
import org.apache.kafka.clients.admin.ListConsumerGroupsResult
import org.apache.kafka.clients.admin.ListOffsetsResult
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo
import org.apache.kafka.clients.admin.OffsetSpec
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.KafkaFuture
import org.apache.kafka.common.TopicPartition
import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Test class for {@link KafkaLagCollector}
 */
class KafkaLagCollectorSpec extends Specification {
    @Subject
    KafkaLagCollector lagCollector

    AdminClient adminClient = Mock()
    ApplicationProperties config = new ApplicationProperties(clusters: [
            new ApplicationProperties.ClusterConfig(
                    name: 'test-cluster',
                    servers: 'localhost:9092'
            )]
    )
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5)
    MeterRegistry meterRegistry = new SimpleMeterRegistry()

    void setup() {
        lagCollector = new KafkaLagCollector(meterRegistry, config, scheduler)
        lagCollector.adminClients['test-cluster'] = adminClient
    }

    void 'test schedule jobs - consumer group names specified'() {
        given:
        String clusterName = 'test-cluster'

        and:
        ScheduledExecutorService mockScheduler = Mock()
        lagCollector = new KafkaLagCollector(meterRegistry, config, mockScheduler)
        config.lagMonitor.clusters.add(new LagMonitorConfig(
                name: clusterName,
                consumerGroups: [
                        'deva-consumer',
                        'test-consumer'
                ]
        ))

        when:
        lagCollector.scheduleJobs()

        then:
        2 * mockScheduler.scheduleAtFixedRate(!null as Runnable, 1, 1, TimeUnit.MINUTES)
    }

    void 'test schedule jobs - consumer group names not specified'() {
        given:
        ScheduledExecutorService mockScheduler = Mock()
        lagCollector = new KafkaLagCollector(meterRegistry, config, mockScheduler)
        config.lagMonitor.clusters.add(new LagMonitorConfig(name: 'test-cluster'))
        config.lagMonitor.clusters.add(new LagMonitorConfig(name: 'deva-cluster'))

        when:
        lagCollector.scheduleJobs()

        then:
        2 * mockScheduler.scheduleAtFixedRate(!null as Runnable, 1, 1, TimeUnit.MINUTES)
    }

    void 'test collect consumer group lag for multiple groups - exception thrown'() {
        given:
        String clusterName = 'test-cluster'
        String groupId = 'test-consumer'

        and:
        LagMonitorConfig lagMonitorConfig = new LagMonitorConfig(name: clusterName)

        and:
        ListConsumerGroupsResult groupsResult = Mock()
        KafkaFuture kafkaFuture = Mock()
        ConsumerGroupListing groupListing = Mock()

        when:
        lagCollector.collectConsumerGroupLag(lagMonitorConfig)

        then:
        1 * adminClient.listConsumerGroups() >> groupsResult
        1 * groupsResult.valid() >> kafkaFuture
        1 * kafkaFuture.get(config.lagMonitor.timeoutSeconds, TimeUnit.SECONDS) >> {
            throw new TimeoutException('test exception')
        }
        0 * groupListing.groupId() >> groupId
        0 * adminClient.listConsumerGroups(groupId)

        then: 'no metrics will be recorded'
        !meterRegistry.meters.any { it.id.name == 'kafka.partition.offset' }
        !meterRegistry.meters.any { it.id.name == 'kafka.consumer.offset' }
        !meterRegistry.meters.any { it.id.name == 'kafka.consumer.lag' }
    }

    void 'test collect consumer group lag for multiple groups - group not present in allow list'() {
        given:
        String clusterName = 'test-cluster'
        String groupId = 'test-consumer'

        and:
        LagMonitorConfig lagMonitorConfig = new LagMonitorConfig(
                name: clusterName,
                groupAllowlist: [
                        '.*deva.*'
                ]
        )
        config.lagMonitor.clusters.add(lagMonitorConfig)
        config.init()

        and:
        ListConsumerGroupsResult groupsResult = Mock()
        KafkaFuture kafkaFuture = Mock()
        ConsumerGroupListing groupListing = Mock()

        when:
        lagCollector.collectConsumerGroupLag(lagMonitorConfig)

        then:
        1 * adminClient.listConsumerGroups() >> groupsResult
        1 * groupsResult.valid() >> kafkaFuture
        1 * kafkaFuture.get(config.lagMonitor.timeoutSeconds, TimeUnit.SECONDS) >> [groupListing]
        1 * groupListing.groupId() >> groupId
        0 * adminClient.listConsumerGroups(groupId)

        then: 'no metrics will be recorded'
        !meterRegistry.meters.any { it.id.name == 'kafka.partition.offset' }
        !meterRegistry.meters.any { it.id.name == 'kafka.consumer.offset' }
        !meterRegistry.meters.any { it.id.name == 'kafka.consumer.lag' }
    }

    void 'test collect consumer group lag for multiple groups - group present in deny list'() {
        given:
        String clusterName = 'test-cluster'
        String groupId = 'test-consumer'

        and:
        LagMonitorConfig lagMonitorConfig = new LagMonitorConfig(
                name: clusterName,
                groupDenylist: [
                        '.*test.*'
                ]
        )
        config.lagMonitor.clusters.add(lagMonitorConfig)
        config.init()

        and:
        ListConsumerGroupsResult groupsResult = Mock()
        KafkaFuture kafkaFuture = Mock()
        ConsumerGroupListing groupListing = Mock()

        when:
        lagCollector.collectConsumerGroupLag(lagMonitorConfig)

        then:
        1 * adminClient.listConsumerGroups() >> groupsResult
        1 * groupsResult.valid() >> kafkaFuture
        1 * kafkaFuture.get(config.lagMonitor.timeoutSeconds, TimeUnit.SECONDS) >> [groupListing]
        1 * groupListing.groupId() >> groupId
        0 * adminClient.listConsumerGroups(groupId)

        then: 'no metrics will be recorded'
        !meterRegistry.meters.any { it.id.name == 'kafka.partition.offset' }
        !meterRegistry.meters.any { it.id.name == 'kafka.consumer.offset' }
        !meterRegistry.meters.any { it.id.name == 'kafka.consumer.lag' }
    }

    void 'test collect consumer group lag for multiple groups'() {
        given:
        String clusterName = 'test-cluster'
        String groupId = 'test-consumer'
        String topicName = 'test-topic'

        and:
        LagMonitorConfig lagMonitorConfig = new LagMonitorConfig(
                name: clusterName,
                groupDenylist: denylist,
                groupAllowlist: allowlist
        )
        config.lagMonitor.clusters.add(lagMonitorConfig)
        config.init()

        and:
        ListConsumerGroupsResult groupsResult = Mock()
        KafkaFuture kafkaFuture = Mock()
        ConsumerGroupListing groupListing = Mock()

        and:
        ListConsumerGroupOffsetsResult groupOffsetsResult = Mock()
        Map<TopicPartition, OffsetSpec> offsetSpecs
        ListOffsetsResult topicOffsetsResult = Mock()

        and:
        def groupOffsets = [
                new TopicPartition(topicName, 0): new OffsetAndMetadata(5),
                new TopicPartition(topicName, 1): new OffsetAndMetadata(6),
                new TopicPartition(topicName, 2): new OffsetAndMetadata(7)
        ]

        when:
        lagCollector.collectConsumerGroupLag(lagMonitorConfig)

        then:
        1 * adminClient.listConsumerGroups() >> groupsResult
        1 * groupsResult.valid() >> kafkaFuture
        1 * kafkaFuture.get(config.lagMonitor.timeoutSeconds, TimeUnit.SECONDS) >> [groupListing]
        2 * groupListing.groupId() >> groupId
        1 * adminClient.listConsumerGroupOffsets(groupId) >> groupOffsetsResult
        1 * groupOffsetsResult.partitionsToOffsetAndMetadata() >> kafkaFuture
        1 * kafkaFuture.get(config.lagMonitor.timeoutSeconds, TimeUnit.SECONDS) >> groupOffsets
        1 * adminClient.listOffsets(!null as Map) >> { params ->
            offsetSpecs = params[0]
            return topicOffsetsResult
        }

        then:
        offsetSpecs.size() == groupOffsets.size()
        (0..(offsetSpecs.size() - 1)).each {
            assert offsetSpecs[(new TopicPartition(topicName, it))] instanceof OffsetSpec.LatestSpec
        }

        then:
        1 * topicOffsetsResult.partitionResult(new TopicPartition(topicName, 0)) >> kafkaFuture
        1 * kafkaFuture.get(config.lagMonitor.timeoutSeconds, TimeUnit.SECONDS) >> new ListOffsetsResultInfo(
                11, System.currentTimeMillis() - 1, Optional.empty()
        )
        1 * topicOffsetsResult.partitionResult(new TopicPartition(topicName, 1)) >> kafkaFuture
        1 * kafkaFuture.get(config.lagMonitor.timeoutSeconds, TimeUnit.SECONDS) >> new ListOffsetsResultInfo(
                15, System.currentTimeMillis() - 2, Optional.empty()
        )
        1 * topicOffsetsResult.partitionResult(new TopicPartition(topicName, 2)) >> kafkaFuture
        1 * kafkaFuture.get(config.lagMonitor.timeoutSeconds, TimeUnit.SECONDS) >> new ListOffsetsResultInfo(
                20, System.currentTimeMillis() - 3, Optional.empty()
        )

        then: 'verify metrics'
        verifyMetrics(topicName, clusterName, groupId, '0', 11, 5)
        verifyMetrics(topicName, clusterName, groupId, '1', 15, 6)
        verifyMetrics(topicName, clusterName, groupId, '2', 20, 7)

        where:
        allowlist << [
                [],
                ['.*test.*'],
                []
        ]
        denylist << [
                [],
                [],
                ['.*deva.*']
        ]
    }

    void 'test collect consumer group lag - exception thrown'() {
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
        1 * kafkaFuture.get(config.lagMonitor.timeoutSeconds, TimeUnit.SECONDS) >> {
            throw new TimeoutException('test exception')
        }

        then: 'no metrics will be recorded'
        !meterRegistry.meters.any { it.id.name == 'kafka.partition.offset' }
        !meterRegistry.meters.any { it.id.name == 'kafka.consumer.offset' }
        !meterRegistry.meters.any { it.id.name == 'kafka.consumer.lag' }
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
        !meterRegistry.meters.any { it.id.name == 'kafka.partition.offset' }
        !meterRegistry.meters.any { it.id.name == 'kafka.consumer.offset' }
        !meterRegistry.meters.any { it.id.name == 'kafka.consumer.lag' }

        where:
        groupOffsets << [
                null,
                [:]
        ]
    }

    void 'test collect consumer group lag - offsets present for all partitions'() {
        given:
        String clusterName = 'test-cluster'
        String groupId = 'test-consumer'
        String topicName = 'test-topic'

        and:
        ListConsumerGroupOffsetsResult groupOffsetsResult = Mock()
        KafkaFuture kafkaFuture = Mock()
        Map<TopicPartition, OffsetSpec> offsetSpecs
        ListOffsetsResult topicOffsetsResult = Mock()

        and:
        def groupOffsets = [
                new TopicPartition(topicName, 0): new OffsetAndMetadata(5),
                new TopicPartition(topicName, 1): new OffsetAndMetadata(6),
                new TopicPartition(topicName, 2): new OffsetAndMetadata(7)
        ]

        when:
        lagCollector.collectConsumerGroupLag(clusterName, groupId)

        then:
        1 * adminClient.listConsumerGroupOffsets(groupId) >> groupOffsetsResult
        1 * groupOffsetsResult.partitionsToOffsetAndMetadata() >> kafkaFuture
        1 * kafkaFuture.get(config.lagMonitor.timeoutSeconds, TimeUnit.SECONDS) >> groupOffsets
        1 * adminClient.listOffsets(!null as Map) >> { params ->
            offsetSpecs = params[0]
            return topicOffsetsResult
        }

        then:
        offsetSpecs.size() == groupOffsets.size()
        (0..(offsetSpecs.size() - 1)).each {
            assert offsetSpecs[(new TopicPartition(topicName, it))] instanceof OffsetSpec.LatestSpec
        }

        then:
        1 * topicOffsetsResult.partitionResult(new TopicPartition(topicName, 0)) >> kafkaFuture
        1 * kafkaFuture.get(config.lagMonitor.timeoutSeconds, TimeUnit.SECONDS) >> new ListOffsetsResultInfo(
                11, System.currentTimeMillis() - 1, Optional.empty()
        )
        1 * topicOffsetsResult.partitionResult(new TopicPartition(topicName, 1)) >> kafkaFuture
        1 * kafkaFuture.get(config.lagMonitor.timeoutSeconds, TimeUnit.SECONDS) >> new ListOffsetsResultInfo(
                15, System.currentTimeMillis() - 2, Optional.empty()
        )
        1 * topicOffsetsResult.partitionResult(new TopicPartition(topicName, 2)) >> kafkaFuture
        1 * kafkaFuture.get(config.lagMonitor.timeoutSeconds, TimeUnit.SECONDS) >> new ListOffsetsResultInfo(
                20, System.currentTimeMillis() - 3, Optional.empty()
        )

        then: 'verify metrics'
        verifyMetrics(topicName, clusterName, groupId, '0', 11, 5)
        verifyMetrics(topicName, clusterName, groupId, '1', 15, 6)
        verifyMetrics(topicName, clusterName, groupId, '2', 20, 7)
    }

    void verifyMetrics(String topicName, String clusterName, String groupId, String partition,
                       long partitionOffset, long consumerOffset) {
        DistributionSummary partitionOffsetMetric = meterRegistry.meters.find {
            it.id.name == 'kafka.partition.offset' &&
                    it.id.tags.any { it.key == KafkaLagCollector.TAG_TOPIC && it.value == topicName } &&
                    it.id.tags.any { it.key == KafkaLagCollector.TAG_PARTITION && it.value == partition } &&
                    it.id.tags.any { it.key == KafkaLagCollector.TAG_CLUSTER_NAME && it.value == clusterName }
        }
        DistributionSummary consumerOffsetMetric = meterRegistry.meters.find {
            it.id.name == 'kafka.consumer.offset' &&
                    it.id.tags.any { it.key == KafkaLagCollector.TAG_TOPIC && it.value == topicName } &&
                    it.id.tags.any { it.key == KafkaLagCollector.TAG_PARTITION && it.value == partition } &&
                    it.id.tags.any { it.key == KafkaLagCollector.TAG_GROUP && it.value == groupId } &&
                    it.id.tags.any { it.key == KafkaLagCollector.TAG_CLUSTER_NAME && it.value == clusterName }
        }
        DistributionSummary consumerLagMetric = meterRegistry.meters.find {
            it.id.name == 'kafka.consumer.lag' &&
                    it.id.tags.any { it.key == KafkaLagCollector.TAG_TOPIC && it.value == topicName } &&
                    it.id.tags.any { it.key == KafkaLagCollector.TAG_PARTITION && it.value == partition } &&
                    it.id.tags.any { it.key == KafkaLagCollector.TAG_GROUP && it.value == groupId } &&
                    it.id.tags.any { it.key == KafkaLagCollector.TAG_CLUSTER_NAME && it.value == clusterName }
        }

        assert partitionOffsetMetric.max() == partitionOffset
        assert consumerOffsetMetric.max() == consumerOffset
        assert consumerLagMetric.max() == partitionOffset - consumerOffset
    }
}
