# Changelog

## [Unreleased]
### Added
- `dependencycheck` gradle plugin to detect vulnerable dependencies
- [#73](https://github.com/devatherock/kafka-lag-monitor/issues/73): Documented configurable properties and environment variables
- Used `circleci-templates` orb to simplify CI pipeline

### Changed
- chore: Added changelog-updater for creating missed changelog entries
- chore(deps): update plugin io.micronaut.application to v2.0.8
- chore(deps): update dependency org.spockframework:spock-core to v2.1-groovy-3.0
- fix(deps): update dependency org.objenesis:objenesis to v3.3
- chore(deps): update plugin com.github.kt3k.coveralls to v2.12.2
- chore(deps): update docker orb to v2.2.0
- fix(deps): update dependency org.jsoup:jsoup to v1.16.1
- Upgraded spotless to `6.19.0`
- Upgraded to Java 17
- Updated dockerhub readme in CI pipeline
- fix(deps): update dependency org.projectlombok:lombok to v1.18.28
- chore(deps): update plugin com.diffplug.spotless to v6.19.0
- fix(deps): update dependency com.fasterxml.jackson.core:jackson-databind to v2.15.2
- chore(deps): update plugin org.sonarqube to v4.2.1.3168
- fix(deps): update dependency ch.qos.logback:logback-classic to v1.4.8
- fix(deps): update dependency org.apache.kafka:kafka-clients to v3.5.0
- chore(deps): update devatherock/simple-slack docker tag to v1
- fix(deps): update dependency net.logstash.logback:logstash-logback-encoder to v7.4
- fix(deps): update dependency io.micronaut:micronaut-bom to v3.9.4
- chore(deps): update dependency gradle to v8.2
- Upgraded `snappy-java` to `1.1.10.1`
- chore(deps): update dependency gradle to v8.2.1
- fix(deps): update dependency org.xerial.snappy:snappy-java to v1.1.10.2
- fix(deps): update dependency org.apache.kafka:kafka-clients to v3.5.1

### Removed
- [#56](https://github.com/devatherock/kafka-lag-monitor/issues/56): Custom environment variables with `LOGGING_LEVEL` prefix and updated documentation to use environment variables with `LOGGER_LEVELS` prefix supported out of the box by micronaut

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