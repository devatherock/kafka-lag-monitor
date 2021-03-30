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

    @Singleton
    @Named("lagMonitorScheduler")
    public ScheduledExecutorService lagMonitorScheduler(ApplicationProperties applicationProperties) {
        return Executors.newScheduledThreadPool(applicationProperties.getLagMonitor().getThreadpoolSize());
    }
}
