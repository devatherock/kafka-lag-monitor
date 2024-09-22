package io.github.devatherock.test

import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

/**
 * Test class for {@link InfluxdbController}
 */
@MicronautTest(environments = 'local')
class InfluxdbControllerSpec extends Specification {

    @Inject
    @Client('/')
    HttpClient httpClient

    @Inject
    InfluxdbController controller

    void cleanup() {
        controller.@metrics = new StringBuilder()
    }

    void 'test get metrics'() {
        given:
        controller.@metrics.append('test line protocol')

        when:
        String output = httpClient.toBlocking().retrieve('/influx/metrics')

        then:
        output.contains('test line protocol')
    }

    void 'test write metrics - multiple writes within 2 seconds'() {
        when:
        httpClient.toBlocking().exchange(HttpRequest.POST(
                UriBuilder.of('/influx/write')
                        .queryParam('consistency', 'one')
                        .queryParam('precision', 'ms')
                        .queryParam('db', 'mydb').build(), 'test line protocol1\n')
                .contentType('text/plain'))
        httpClient.toBlocking().exchange(HttpRequest.POST(UriBuilder.of('/influx/write')
                .queryParam('consistency', 'one')
                .queryParam('precision', 'ms')
                .queryParam('db', 'mydb').build(), 'test line protocol2')
                .contentType('text/plain'))

        then:
        controller.metrics.contains('test line protocol2')
    }

    void 'test write metrics - multiple writes more than 2 seconds apart'() {
        when:
        httpClient.toBlocking().exchange(HttpRequest.POST(
                UriBuilder.of('/influx/write')
                        .queryParam('consistency', 'one')
                        .queryParam('precision', 'ms')
                        .queryParam('db', 'mydb').build(), 'test line protocol1\n')
                .contentType('text/plain'))
        Thread.sleep(3000)
        httpClient.toBlocking().exchange(HttpRequest.POST(UriBuilder.of('/influx/write')
                .queryParam('consistency', 'one')
                .queryParam('precision', 'ms')
                .queryParam('db', 'mydb').build(), 'test line protocol2')
                .contentType('text/plain'))

        then:
        !controller.metrics.contains('test line protocol1')
        controller.metrics.contains('test line protocol2')
    }

    void 'test other query'() {
        when:
        httpClient.toBlocking().exchange(HttpRequest.POST('/influx/query',
                'q=' + URLEncoder.encode('CREATE DATABASE "mydb"', 'UTF-8'))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE))

        then:
        noExceptionThrown()
    }
}
