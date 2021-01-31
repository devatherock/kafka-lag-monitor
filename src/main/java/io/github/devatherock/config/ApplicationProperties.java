package io.github.devatherock.config;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import lombok.Getter;
import lombok.Setter;

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

    @Getter
    @Setter
    public static class LagMonitorConfig {
        @NotBlank(message = "kafka.lag-monitor.clusters[*].name not specified")
        private String name;

        private List<String> consumerGroups = new ArrayList<>();
    }
}
