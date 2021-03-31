package io.github.devatherock.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Named;
import javax.inject.Singleton;

import io.micronaut.context.annotation.Factory;

/**
 * Bean definitions for the application
 */
@Factory
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
