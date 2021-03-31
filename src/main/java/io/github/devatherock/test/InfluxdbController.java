package io.github.devatherock.test;

import io.micrometer.influx.InfluxMeterRegistry;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Intended for usage in local, to log the metrics published
 */
@Slf4j
@Hidden
@Requires(property = "micronaut.metrics.export.influx.enabled")
@Controller("/influx")
@RequiredArgsConstructor
public class InfluxdbController {
    private StringBuilder metrics = new StringBuilder();
    private long lastRequestTime = System.currentTimeMillis();

    /**
     * Captures metrics published by {@link InfluxMeterRegistry}
     *
     * @param payload
     */
    @Post(value = "/write", consumes = { MediaType.TEXT_PLAIN })
    @Status(HttpStatus.CREATED)
    public void influxMetrics(@Body String payload) {
        long currentTime = System.currentTimeMillis();

        // If last request was within 2 seconds, assume this request is part of the same
        // batch
        if (currentTime - lastRequestTime < 2000) {
            metrics.append(payload);
        } else {
            metrics = new StringBuilder(payload);
        }
        lastRequestTime = currentTime;
        LOGGER.debug("Metrics written to influx");
    }

    /**
     * Catch-all method for other requests triggered from
     * {@link InfluxMeterRegistry}
     *
     * @param path
     */
    @Post(value = "/{path}", consumes = { MediaType.APPLICATION_FORM_URLENCODED })
    public void influx(@PathVariable String path) {
        LOGGER.debug("Request path: {}", path);
    }

    /**
     * Returns last reported metrics during development, to provide equivalent
     * functionality to the {@code /prometheus} endpoint
     *
     * @return payload
     */
    @Get(value = "/metrics", produces = { MediaType.TEXT_PLAIN })
    public String getMetrics() {
        return metrics.toString();
    }
}
