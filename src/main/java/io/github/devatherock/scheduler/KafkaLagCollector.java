package io.github.devatherock.scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import io.github.devatherock.config.ApplicationProperties;
import io.github.devatherock.config.ApplicationProperties.LagMonitorConfig;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.context.annotation.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Context
@Singleton
@RequiredArgsConstructor
public class KafkaLagCollector {
    private static final String TAG_TOPIC = "topic";
    private static final String TAG_GROUP = "group";
    private static final String TAG_PARTITION = "partition";
    private static final String TAG_CLUSTER_NAME = "cluster_name";
    
    private final MeterRegistry meterRegistry;
    private final ApplicationProperties config;
    private final ScheduledExecutorService scheduler;
    private final Map<String, Admin> adminClients = new HashMap<>();

    @PostConstruct
    public void init() throws InterruptedException, ExecutionException, TimeoutException {
        config.getClusters().forEach(clusterConfig -> {
            Properties props = new Properties();
            props.put("bootstrap.servers", clusterConfig.getServers());
            Admin adminClient = Admin.create(props);
            adminClients.put(clusterConfig.getName(), adminClient);
        });

        for (LagMonitorConfig lagMonitorConfig : config.getLagMonitor().getClusters()) {
            Admin adminClient = adminClients.get(lagMonitorConfig.getName());
            List<String> groupIds = null;

            if (!lagMonitorConfig.getConsumerGroups().isEmpty()) {
                groupIds = lagMonitorConfig.getConsumerGroups();
            } else {
                groupIds = adminClient.listConsumerGroups().valid()
                        .get(config.getLagMonitor().getTimeoutSeconds(), TimeUnit.SECONDS).stream()
                        .map(consumerGroup -> consumerGroup.groupId())
                        .collect(Collectors.toList());
            }

            for (String groupId : groupIds) {
                scheduler.scheduleAtFixedRate(() -> {
                    collectConsumerGroupLag(lagMonitorConfig.getName(), groupId);
                }, 1, 1, TimeUnit.MINUTES);
            }
        }
    }

    private void collectConsumerGroupLag(String clusterName, String groupId) {
        LOGGER.debug("Collecting metrics for consumer group '{}'", groupId);
        Admin adminClient = adminClients.get(clusterName);

        try {
            ListConsumerGroupOffsetsResult groupOffsetsResult = adminClient
                    .listConsumerGroupOffsets(groupId);
            Map<TopicPartition, OffsetAndMetadata> groupOffsets = groupOffsetsResult
                    .partitionsToOffsetAndMetadata()
                    .get(config.getLagMonitor().getTimeoutSeconds(), TimeUnit.SECONDS);
            if (null != groupOffsets && !groupOffsets.isEmpty()) {
                Map<TopicPartition, OffsetSpec> offsetSpecs = groupOffsets.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, (entry) -> OffsetSpec.latest()));
                ListOffsetsResult topicOffsetsResult = adminClient.listOffsets(offsetSpecs);

                for (Map.Entry<TopicPartition, OffsetAndMetadata> groupOffset : groupOffsets.entrySet()) {
                    ListOffsetsResultInfo partitionOffsetResult = topicOffsetsResult
                            .partitionResult(groupOffset.getKey())
                            .get(config.getLagMonitor().getTimeoutSeconds(), TimeUnit.SECONDS);

                    // Latest partition offset
                    DistributionSummary
                            .builder("kafka.partition.offset")
                            .tag(TAG_TOPIC, groupOffset.getKey().topic())
                            .tag(TAG_PARTITION, String.valueOf(groupOffset.getKey().partition()))
                            .tag(TAG_CLUSTER_NAME, clusterName)
                            .register(meterRegistry)
                            .record(partitionOffsetResult.offset());

                    // Latest consumer group offset
                    DistributionSummary
                            .builder("kafka.consumer.offset")
                            .tag(TAG_TOPIC, groupOffset.getKey().topic())
                            .tag(TAG_PARTITION, String.valueOf(groupOffset.getKey().partition()))
                            .tag(TAG_GROUP, groupId)
                            .tag(TAG_CLUSTER_NAME, clusterName)
                            .register(meterRegistry)
                            .record(groupOffset.getValue().offset());

                    // Lag
                    DistributionSummary
                            .builder("kafka.consumer.lag")
                            .tag(TAG_TOPIC, groupOffset.getKey().topic())
                            .tag(TAG_PARTITION, String.valueOf(groupOffset.getKey().partition()))
                            .tag(TAG_GROUP, groupId)
                            .tag(TAG_CLUSTER_NAME, clusterName)
                            .register(meterRegistry)
                            .record(partitionOffsetResult.offset() - groupOffset.getValue().offset());
                }
                LOGGER.debug("Metrics collected for consumer group '{}'", groupId);
            } else {
                LOGGER.info("Offsets not found for consumer group '{}'", groupId);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Group Id: {}, Exception: {}, Message: {}", groupId, e.getClass().getName(),
                    e.getMessage());
        }
    }
}
