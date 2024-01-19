package io.github.devatherock.controllers

import javax.inject.Inject

import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test for additional endpoints like {@code /health}
 */
abstract class AdditionalControllerSpec extends Specification {

    @Inject
    @Client('${test.server.url}')
    HttpClient httpClient

    @Unroll
    void 'test endpoint - #endpoint'() {
        when:
        HttpResponse response = httpClient.toBlocking().exchange(endpoint)

        then:
        response.status.code == 200

        where:
        endpoint << [
                '/health',
                '/metrics',
                '/swagger/kafka-lag-monitor-v1.yml'
        ]
    }
}
