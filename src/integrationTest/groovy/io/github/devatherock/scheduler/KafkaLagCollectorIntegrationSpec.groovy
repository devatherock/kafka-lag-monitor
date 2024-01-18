package io.github.devatherock.scheduler

import io.micronaut.test.extensions.spock.annotation.MicronautTest

/**
 * Integration test for {@link KafkaLagCollector}
 */
@MicronautTest(propertySources = 'classpath:application-integration.yml', startApplication = false)
class KafkaLagCollectorIntegrationSpec extends KafkaLagCollectorBaseSpec {
}
