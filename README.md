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