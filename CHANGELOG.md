# Changelog

## [Unreleased]
### Added
- Used an influxdb container for tests

### Changed
- chore(deps): update plugin com.diffplug.spotless to v6.25.0
- fix(deps): update dependency org.apache.kafka:kafka-clients to v3.7.0
- chore(deps): update bitnami/kafka docker tag to v3.7.0
- fix(deps): update dependency com.fasterxml.jackson.core:jackson-databind to v2.17.0
- fix(deps): update dependency org.projectlombok:lombok to v1.18.32
- chore(deps): update dependency gradle to v8.7
- fix(deps): update dependency net.bytebuddy:byte-buddy to v1.14.13
- chore(deps): update plugin org.sonarqube to v5
- chore(deps): update cimg/openjdk docker tag to v17.0.11
- fix(deps): update dependency ch.qos.logback:logback-classic to v1.5.6
- chore(deps): update templates orb to v0.6.0
- Triggered slack notification on pipeline failure as well
- Used multi-arch `influxdb` docker image

## [1.0.0] - 2024-01-21
### Added
- [#73](https://github.com/devatherock/kafka-lag-monitor/issues/73): Documented configurable properties and environment variables
- Used `circleci-templates` orb to simplify CI pipeline
- [#172](https://github.com/devatherock/kafka-lag-monitor/issues/172): Added integration tests
- [#222](https://github.com/devatherock/kafka-lag-monitor/issues/222): Built a graalvm native image
- [#176](https://github.com/devatherock/kafka-lag-monitor/issues/176): Built separate x86 and arm64 docker images

### Changed
- chore: Added changelog-updater for creating missed changelog entries
- fix(deps): update dependency org.objenesis:objenesis to v3.3
- chore(deps): update plugin com.github.kt3k.coveralls to v2.12.2
- fix(deps): update dependency org.jsoup:jsoup to v1.16.1
- Upgraded to Java 17
- Updated dockerhub readme in CI pipeline
- chore(deps): update devatherock/simple-slack docker tag to v1
- fix(deps): update dependency net.logstash.logback:logstash-logback-encoder to v7.4
- fix(deps): update dependency org.apache.kafka:kafka-clients to v3.5.1
- chore(deps): update dependency gradle to v8.3
- chore(deps): update plugin org.sonarqube to v4.3.1.3277
- fix(deps): update dependency org.projectlombok:lombok to v1.18.30
- fix(deps): update dependency org.xerial.snappy:snappy-java to v1.1.10.5
- fix(deps): update dependency ch.qos.logback:logback-classic to v1.4.14
- fix(deps): update dependency com.fasterxml.jackson.core:jackson-databind to v2.16.1
- fix(deps): update dependency net.bytebuddy:byte-buddy to v1.14.11
- Upgraded `spotless` to `6.24.0`
- chore(deps): update dependency gradle to v8.5
- chore(deps): update plugin org.sonarqube to v4.4.1.3373
- fix(deps): update dependency org.apache.kafka:kafka-clients to v3.6.1
- fix(deps): update dependency org.jsoup:jsoup to v1.17.2

### Removed
- [#56](https://github.com/devatherock/kafka-lag-monitor/issues/56): Custom environment variables with `LOGGING_LEVEL` prefix and updated documentation to use environment variables with `LOGGER_LEVELS` prefix supported out of the box by micronaut
- Docker orb

## [0.2.0] - 2021-04-01
### Added
- Spotless gradle plugin to format code
- Support for JSON logs

### Changed
- `/influx/metrics` endpoint to work correctly when metrics are reported in multiple batches

## [0.1.1] - 2021-03-30
### Changed
- [#9](https://github.com/devatherock/kafka-lag-monitor/issues/9): Fixed performance issues due to the lag collector job running in the event loop

## [0.1.0] - 2021-03-29
### Added
- Initial version. Records kafka lag for specified consumer groups
- [#4](https://github.com/devatherock/kafka-lag-monitor/issues/4): Accepted regular expressions in the list of consumer groups to monitor
- [#5](https://github.com/devatherock/kafka-lag-monitor/issues/5): Accept a list of regular expressions for consumer groups to exclude