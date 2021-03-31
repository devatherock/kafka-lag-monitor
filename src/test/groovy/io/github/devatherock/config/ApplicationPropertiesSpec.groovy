package io.github.devatherock.config

import spock.lang.Specification
import spock.lang.Subject

/**
 * Test class for {@link ApplicationProperties}
 */
class ApplicationPropertiesSpec extends Specification {
    @Subject
    ApplicationProperties applicationProperties = new ApplicationProperties()

    void 'test initialize'() {
        given:
        applicationProperties.lagMonitor.clusters = [
                new ApplicationProperties.LagMonitorConfig(
                        name: 'test-cluster',
                        groupDenylist: [
                                '.*test.*',
                                'console-consumer.*'
                        ],
                        groupAllowlist: [
                                '.*deva.*',
                                '.*rock.*'
                        ]
                )
        ]

        when:
        applicationProperties.init()

        then:
        applicationProperties.lagMonitor.clusters[0].groupDenylistCompiled.size() == 2
        applicationProperties.lagMonitor.clusters[0].groupDenylistCompiled[0].pattern() == '.*test.*'
        applicationProperties.lagMonitor.clusters[0].groupDenylistCompiled[1].pattern() == 'console-consumer.*'
        applicationProperties.lagMonitor.clusters[0].groupAllowlistCompiled[0].pattern() == '.*deva.*'
        applicationProperties.lagMonitor.clusters[0].groupAllowlistCompiled[1].pattern() == '.*rock.*'
    }
}
