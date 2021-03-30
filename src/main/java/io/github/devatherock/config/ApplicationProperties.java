package io.github.devatherock.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
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
    @ConfigurationProperties("lag-monitor")
    public static class LagMonitorProperties {
        @NotEmpty
        private List<LagMonitorConfig> clusters = new ArrayList<>();

        /**
         * Size of the thread pool used by the lag monitor
         */
        private int threadpoolSize = 5;

        /**
         * Timeout for the requests to Kafka
         */
        private int timeoutSeconds = 5;
    }

    @Getter
    @Setter
    public static class ClusterConfig {
        @NotBlank(message = "kafka.clusters[*].name not specified")
        private String name;

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
    public static class LagMonitorConfig {
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
