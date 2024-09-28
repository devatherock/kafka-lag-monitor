package io.github.devatherock.controllers

import java.nio.file.Files
import java.nio.file.Paths

import io.micronaut.test.extensions.spock.annotation.MicronautTest

/**
 * Integration test for additional endpoints
 */
@MicronautTest(propertySources = 'classpath:application-integration.yml', startApplication = false)
class AdditionalControllerIntegrationSpec extends AdditionalControllerSpec {
    static final String REMOTE_LOG_CONFIG =
        'https://raw.githubusercontent.com/devatherock/ldap-search-api/master/src/main/resources/logback.xml'
    static final LOG_MAP = [
        'logback-json.xml': '"message":"Startup completed',
        (REMOTE_LOG_CONFIG): 'INFO  io.micronaut.runtime.Micronaut - Startup completed',
    ]

    void 'test log format'() {
        expect:
        Files.readString(Paths.get('logs-intg.txt'))
                .contains(LOG_MAP[System.getenv('LOGGING_CONFIG')])
    }
}
