[![CircleCI](https://circleci.com/gh/devatherock/kafka-lag-monitor.svg?style=svg)](https://circleci.com/gh/devatherock/kafka-lag-monitor)
[![Coverage Status](https://coveralls.io/repos/github/devatherock/kafka-lag-monitor/badge.svg?branch=master)](https://coveralls.io/github/devatherock/kafka-lag-monitor?branch=master)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=kafka-lag-monitor&metric=alert_status)](https://sonarcloud.io/component_measures?id=kafka-lag-monitor&metric=alert_status&view=list)
[![Docker Pulls](https://img.shields.io/docker/pulls/devatherock/kafka-lag-monitor.svg)](https://hub.docker.com/r/devatherock/kafka-lag-monitor/)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=kafka-lag-monitor&metric=ncloc)](https://sonarcloud.io/component_measures?id=kafka-lag-monitor&metric=ncloc)
[![Docker Image Size](https://img.shields.io/docker/image-size/devatherock/kafka-lag-monitor.svg?sort=date)](https://hub.docker.com/r/devatherock/kafka-lag-monitor/)
# kafka-lag-monitor
Monitors kafka lag and publishes the metrics to different metrics backends

## Metrics
The supported metrics backends are Prometheus and InfluxDB

### Sample metrics

**Prometheus:**

The metrics in Prometheus format can be accessed at `/prometheus` endpoint

```text
# HELP kafka_consumer_lag_max  
# TYPE kafka_consumer_lag_max gauge
kafka_consumer_lag_max{cluster_name="test-cluster",group="test-consumer",partition="1",topic="test-topic",} 2.0
kafka_consumer_lag_max{cluster_name="test-cluster",group="test-consumer",partition="0",topic="test-topic",} 2.0
# HELP kafka_consumer_lag  
# TYPE kafka_consumer_lag summary
kafka_consumer_lag_count{cluster_name="test-cluster",group="test-consumer",partition="1",topic="test-topic",} 1.0
kafka_consumer_lag_sum{cluster_name="test-cluster",group="test-consumer",partition="1",topic="test-topic",} 2.0
kafka_consumer_lag_count{cluster_name="test-cluster",group="test-consumer",partition="0",topic="test-topic",} 1.0
kafka_consumer_lag_sum{cluster_name="test-cluster",group="test-consumer",partition="0",topic="test-topic",} 2.0
# HELP kafka_consumer_offset  
# TYPE kafka_consumer_offset summary
kafka_consumer_offset_count{cluster_name="test-cluster",group="test-consumer",partition="1",topic="test-topic",} 1.0
kafka_consumer_offset_sum{cluster_name="test-cluster",group="test-consumer",partition="1",topic="test-topic",} 16.0
kafka_consumer_offset_count{cluster_name="test-cluster",group="test-consumer",partition="0",topic="test-topic",} 1.0
kafka_consumer_offset_sum{cluster_name="test-cluster",group="test-consumer",partition="0",topic="test-topic",} 13.0
# HELP kafka_consumer_offset_max  
# TYPE kafka_consumer_offset_max gauge
kafka_consumer_offset_max{cluster_name="test-cluster",group="test-consumer",partition="1",topic="test-topic",} 16.0
kafka_consumer_offset_max{cluster_name="test-cluster",group="test-consumer",partition="0",topic="test-topic",} 13.0
# HELP kafka_partition_offset  
# TYPE kafka_partition_offset summary
kafka_partition_offset_count{cluster_name="test-cluster",partition="1",topic="test-topic",} 1.0
kafka_partition_offset_sum{cluster_name="test-cluster",partition="1",topic="test-topic",} 18.0
kafka_partition_offset_count{cluster_name="test-cluster",partition="0",topic="test-topic",} 1.0
kafka_partition_offset_sum{cluster_name="test-cluster",partition="0",topic="test-topic",} 15.0
# HELP kafka_partition_offset_max  
# TYPE kafka_partition_offset_max gauge
kafka_partition_offset_max{cluster_name="test-cluster",partition="1",topic="test-topic",} 18.0
kafka_partition_offset_max{cluster_name="test-cluster",partition="0",topic="test-topic",} 15.0
```

**Influxdb:**

Metrics in InfluxDB's line protocol format will be reported by default to `http://localhost:8086/write` endpoint, every minute

```text
kafka_consumer_lag,cluster_name=test-cluster,group=test-consumer,partition=0,topic=test-topic,metric_type=histogram sum=2,count=1,mean=2,upper=2 1612125711313
kafka_consumer_lag,cluster_name=test-cluster,group=test-consumer,partition=1,topic=test-topic,metric_type=histogram sum=2,count=1,mean=2,upper=2 1612125711311
kafka_consumer_offset,cluster_name=test-cluster,group=test-consumer,partition=0,topic=test-topic,metric_type=histogram sum=13,count=1,mean=13,upper=13 1612125711307
kafka_consumer_offset,cluster_name=test-cluster,group=test-consumer,partition=1,topic=test-topic,metric_type=histogram sum=16,count=1,mean=16,upper=16 1612125711308
kafka_partition_offset,cluster_name=test-cluster,partition=0,topic=test-topic,metric_type=histogram sum=15,count=1,mean=15,upper=15 1612125711311
kafka_partition_offset,cluster_name=test-cluster,partition=1,topic=test-topic,metric_type=histogram sum=18,count=1,mean=18,upper=18 1612125711313
```

## Usage

```
docker run --rm \
        -p 8080:8080  \
        -v /path/to/config:/config \
        -e MICRONAUT_CONFIG_FILES=/config/application.yml \
        -e MICRONAUT_METRICS_EXPORT_INFLUX_ENABLED=false \
        devatherock/kafka-lag-monitor:latest
```

## Configurable properties

### application.yml variables

```yaml
kafka:
  clusters: # Required. A list of kafka cluster definitions
    - name: test-cluster # Required. Name of the cluster. The same name will be needed in `kafka.lag-monitor.clusters[*].name` config. 
      servers: test-cluster.test.com:9092 # Required. The server(s)/broker(s) that belong to this cluster
  lag-monitor:
    clusters:
      - name: test-cluster # Required. Name of the cluster to monitor. Should be one of the defined `kafka.clusters[*].name`
        consumer-groups: # Optional. List of consumer group names to monitor. Names will be matched exactly. Use `group-allowlist` for regex match
          - test-consumer
        group-allowlist: # Optional. List of regular expressions to match against consumer group names to monitor. Will be ignored if `consumer-groups` is specified
          - deva.*
        group-denylist: # Optional. List of regular expressions to match against consumer group names to exclude. Will be ignored if `consumer-groups` or `group-allowlist` is specified
          - temp.*
    threadpool-size: 5 # Optional. Size of the thread pool used by the lag monitor. Defaults to 5
    timeout-seconds: 5 # Optional. Timeout for the requests to Kafka, in seconds. Defaults to 5
    initial-delay-seconds: 60 # Optional. Initial delay before metric collection begins, in seconds. Defaults to 60
    interval-seconds: 60 # Optional. Metric collection interval, in seconds. Defaults to 60
micronaut:
  server:
    port: 8080 # Optional. Port in which the app listens on
```

### Environment variables

| Environment Variable Name             | Required | Default | Description                                                                                                         |
|---------------------------------------|----------|---------|---------------------------------------------------------------------------------------------------------------------|
| KAFKA_LAG_MONITOR_THREADPOOL_SIZE     | false    | 5       | Size of the thread pool used by the lag monitor                                                                     |
| KAFKA_LAG_MONITOR_TIMEOUT_SECONDS     | false    | 5       | Timeout for the requests to Kafka, in seconds                                                                       |
| LOGGER_LEVELS_ROOT                    | false    | INFO    | [SLF4J](http://www.slf4j.org/api/org/apache/commons/logging/Log.html) log level, for all(framework and custom) code |
| LOGGER_LEVELS_IO_GITHUB_DEVATHEROCK   | false    | INFO    | [SLF4J](http://www.slf4j.org/api/org/apache/commons/logging/Log.html) log level, for custom code                    |
| MICRONAUT_SERVER_PORT                 | false    | 8080    | Port in which the app listens on                                                                                    |
| MICRONAUT_CONFIG_FILES                | true     | (None)  | Path to YAML config files. The YAML files can be used to specify complex, object and array properties               |
| JAVA_OPTS                             | false    | (None)  | Additional JVM arguments to be passed to the container's java process                                               |
| LOGBACK_CONFIGURATION_FILE            | false    | (None)  | Path to logback configuration file                                                                                  |

## Troubleshooting
### Enabling debug logs
- Set the environment variable `LOGGER_LEVELS_ROOT` to `DEBUG` to enable all debug logs - custom and framework
- Set the environment variable `LOGGER_LEVELS_IO_GITHUB_DEVATHEROCK` to `DEBUG` to enable debug logs only in custom code
- For fine-grained logging control, supply a custom [logback.xml](http://logback.qos.ch/manual/configuration.html) file
and set the environment variable `LOGBACK_CONFIGURATION_FILE` to `/path/to/custom/logback.xml`

### JSON logs

To output logs as JSON, set the environment variable `LOGBACK_CONFIGURATION_FILE` to `logback-json.xml`. Refer
[logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder) documentation to customize the field names and 
formats in the log