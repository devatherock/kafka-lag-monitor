package io.github.devatherock.config

import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.ScheduledExecutorService

/**
 * Test class for {@link AppConfig}
 */
class AppConfigSpec extends Specification {
    @Subject
    AppConfig appConfig = new AppConfig()

    void 'test initialize scheduler'() {
        given:
        ApplicationProperties applicationProperties = new ApplicationProperties()

        when:
        ScheduledExecutorService executorService = appConfig.lagMonitorScheduler(applicationProperties)

        then:
        executorService.corePoolSize == applicationProperties.lagMonitor.threadpoolSize
    }
}
