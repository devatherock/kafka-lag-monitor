package io.github.devatherock.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import io.micronaut.context.annotation.Factory;
import io.micronaut.core.annotation.ReflectionConfig;
import io.micronaut.core.annotation.ReflectionConfig.ReflectiveMethodConfig;
import io.micronaut.core.annotation.TypeHint.AccessType;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import net.logstash.logback.encoder.LogstashEncoder;

/**
 * Bean definitions for the application
 */
@Factory
@ReflectionConfig(type = LogstashEncoder.class, accessType = AccessType.ALL_PUBLIC_METHODS, methods = {
        @ReflectiveMethodConfig(name = "<init>")
})
public class AppConfig {

    /**
     * Initializes a {@link ScheduledExecutorService} to run the lag collector job
     * in
     * 
     * @param applicationProperties
     * @return {@link ScheduledExecutorService}
     */
    @Named("lagMonitorScheduler")
    @Singleton
    public ScheduledExecutorService lagMonitorScheduler(ApplicationProperties applicationProperties) {
        return Executors.newScheduledThreadPool(applicationProperties.getLagMonitor().getThreadpoolSize());
    }
}
