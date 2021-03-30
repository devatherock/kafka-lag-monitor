package io.github.devatherock.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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

/**
 * Class to collect kafka lag periodically
 * 
 * @author devaprasadh
 *
 */
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
    public void init() {
        config.getClusters().forEach(clusterConfig -> {
            Properties props = new Properties();
            props.put("bootstrap.servers", clusterConfig.getServers());
            Admin adminClient = Admin.create(props);
            adminClients.put(clusterConfig.getName(), adminClient);
        });

        scheduleJobs();
    }

    /**
     * Schedules one or more jobs to collect lag
     */
    private void scheduleJobs() {
        for (LagMonitorConfig lagMonitorConfig : config.getLagMonitor().getClusters()) {
            if (!lagMonitorConfig.getConsumerGroups().isEmpty()) {
                for (String groupId : lagMonitorConfig.getConsumerGroups()) {
                    scheduler.scheduleAtFixedRate(() -> {
                        collectConsumerGroupLag(lagMonitorConfig.getName(), groupId);
                    }, 1, 1, TimeUnit.MINUTES);
                }
            } else {
                scheduler.scheduleAtFixedRate(() -> {
                    collectConsumerGroupLag(lagMonitorConfig);
                }, 1, 1, TimeUnit.MINUTES);
            }
        }
    }

    /**
     * Collects the lag for all allowed consumer groups in a cluster
     * 
     * @param lagMonitorConfig
     */
    private void collectConsumerGroupLag(LagMonitorConfig lagMonitorConfig) {
        List<Future<?>> futures = new ArrayList<>();
        Admin adminClient = adminClients.get(lagMonitorConfig.getName());

        try {
            adminClient.listConsumerGroups().valid()
                    .get(config.getLagMonitor().getTimeoutSeconds(), TimeUnit.SECONDS).stream()
                    .filter(group -> isAllowedConsumerGroup(lagMonitorConfig, group.groupId()))
                    .forEach(group -> {
                        futures.add(scheduler
                                .submit(() -> collectConsumerGroupLag(lagMonitorConfig.getName(), group.groupId())));
                    });

            for (Future<?> future : futures) {
                future.get();
            }
        } catch (ExecutionException | TimeoutException exception) {
            LOGGER.error("Exception: {}, Message: {}", exception.getClass().getName(),
                    exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Collects the lag for a specific consumer group
     * 
     * @param clusterName
     * @param groupId
     */
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
        } catch (ExecutionException | TimeoutException exception) {
            LOGGER.error("Group Id: {}, Exception: {}, Message: {}", groupId, exception.getClass().getName(),
                    exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Checks if the supplied consumer group name is allowed for monitoring
     * 
     * @param config
     * @param consumerGroup
     * @return a flag
     */
    private boolean isAllowedConsumerGroup(LagMonitorConfig config, String consumerGroup) {
        boolean isAllowed = false;

        if (config.getGroupAllowlistCompiled().isEmpty()) {
            if (!config.getGroupDenylistCompiled().isEmpty()) {
                if (!config.getGroupDenylistCompiled().stream()
                        .anyMatch(pattern -> pattern.matcher(consumerGroup).matches())) {
                    isAllowed = true;
                }
            } else {
                isAllowed = true;
            }
        } else if (config.getGroupAllowlistCompiled().stream()
                .anyMatch(pattern -> pattern.matcher(consumerGroup).matches())) {
            isAllowed = true;
        }

        return isAllowed;
    }
}
