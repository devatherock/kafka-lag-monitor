package io.github.devatherock.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.annotation.Introspected;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for the application
 * 
 * @author devaprasadh
 *
 */
@Getter
@Setter
@Context
@ConfigurationProperties("kafka")
public class ApplicationProperties {
    /**
     * A list of kafka cluster definitions
     */
    @NotEmpty
    private List<ClusterConfig> clusters = new ArrayList<>();

    @Valid
    private LagMonitorProperties lagMonitor = new LagMonitorProperties();

    /**
     * Lag monitor configuration
     * 
     * @author devaprasadh
     */
    @Getter
    @Setter
    @Introspected
    @ConfigurationProperties("lag-monitor")
    public static class LagMonitorProperties {
        /**
         * A list of cluster level lag monitor definitions
         */
        @NotEmpty
        private List<LagMonitorConfig> clusters = new ArrayList<>();

        /**
         * Size of the thread pool used by the lag monitor
         */
        private int threadpoolSize = 5;

        /**
         * Timeout for the requests to Kafka, in seconds
         */
        private int timeoutSeconds = 5;

        /**
         * Initial delay before metric collection begins, in seconds
         */
        private int initialDelaySeconds = 60;

        /**
         * Metric collection interval, in seconds
         */
        private int intervalSeconds = 60;
    }

    @Getter
    @Setter
    @Introspected
    public static class ClusterConfig {
        /**
         * Name of the cluster. The same name will be needed in
         * {@code kafka.lag-monitor.clusters[*].name} config
         */
        @NotBlank(message = "kafka.clusters[*].name not specified")
        private String name;

        /**
         * The server(s)/broker(s) that belong to this cluster
         */
        @NotBlank(message = "kafka.clusters[*].servers not specified")
        private String servers;
    }

    /**
     * Lag monitor configuration for a single kafka cluster
     * 
     * @author devaprasadh
     *
     */
    @Getter
    @Setter
    @Introspected
    public static class LagMonitorConfig {
        /**
         * Name of the cluster to monitor. Should be one of the defined
         * {@code kafka.clusters[*].name}
         */
        @NotBlank(message = "kafka.lag-monitor.clusters[*].name not specified")
        private String name;

        /**
         * List of consumer group names to monitor. Names will be matched exactly. Use
         * {@link #groupAllowlist} for regex match
         */
        private List<String> consumerGroups = new ArrayList<>();

        /**
         * List of regular expressions to match against consumer group names to monitor.
         * Will be ignored if {@link #consumerGroups} is specified
         */
        private List<String> groupAllowlist = new ArrayList<>();
        @Setter(AccessLevel.NONE)
        private List<Pattern> groupAllowlistCompiled = new ArrayList<>();

        /**
         * List of regular expressions to match against consumer group names to exclude.
         * Will be ignored if {@link #consumerGroups} or { #groupAllowlist} is specified
         */
        private List<String> groupDenylist = new ArrayList<>();
        @Setter(AccessLevel.NONE)
        private List<Pattern> groupDenylistCompiled = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        lagMonitor.clusters.forEach(lagMonitorConfig -> {
            lagMonitorConfig.groupAllowlist.forEach(
                    allowedGroup -> lagMonitorConfig.groupAllowlistCompiled.add(Pattern.compile(allowedGroup)));
            lagMonitorConfig.groupDenylist.forEach(
                    deniedGroup -> lagMonitorConfig.groupDenylistCompiled.add(Pattern.compile(deniedGroup)));
        });
    }
}
