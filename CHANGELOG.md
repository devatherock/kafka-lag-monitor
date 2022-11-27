# Changelog

## [Unreleased]
### Added
- `dependencycheck` gradle plugin to detect vulnerable dependencies
- [#73](https://github.com/devatherock/kafka-lag-monitor/issues/73): Documented configurable properties and environment variables

### Changed
- chore(deps): update dependency org.apache.kafka:kafka-clients to v2.8.1
- chore: Added changelog-updater for creating missed changelog entries
- chore(deps): update dependency org.projectlombok:lombok to v1.18.22
- chore(deps): update plugin io.micronaut.application to v2.0.8
- chore(deps): update docker orb to v2.0.1
- chore(deps): update plugin org.owasp.dependencycheck to 7.0.3
- chore(deps): update dependency net.logstash.logback:logstash-logback-encoder to v7
- chore(deps): update plugin com.diffplug.spotless to v6.0.1
- chore(deps): updated logback to `1.2.10`
- Upgraded micronaut to `3.4.1`
- chore(deps): update dependency ch.qos.logback:logback-classic to v1.2.11
- chore(deps): update dependency gradle to v6.9.2
- chore(deps): update docker orb to v2.0.3
- chore(deps): update plugin org.owasp.dependencycheck to v7.0.4.1
- chore(deps): update dependency net.logstash.logback:logstash-logback-encoder to v7.1
- chore(deps): update dependency io.micronaut:micronaut-bom to v3.4.2
- chore(deps): update dependency net.logstash.logback:logstash-logback-encoder to v7.1.1
- chore(deps): update dependency org.spockframework:spock-core to v2.1-groovy-3.0
- chore(deps): update plugin com.diffplug.spotless to v6.4.2
- chore(deps): update dependency cimg/openjdk to v17
- chore: Used custom ssh key to push to github
- chore(deps): update dependency io.micronaut:micronaut-bom to v3.4.3
- chore(deps): update dependency org.projectlombok:lombok to v1.18.24
- chore(deps): update docker orb to v2.1.1
- chore(deps): update plugin com.diffplug.spotless to v6.5.2
- chore(deps): update plugin org.owasp.dependencycheck to v7.1.0.1
- chore(deps): update dependency io.micronaut:micronaut-bom to v3.4.4
- chore(deps): update dependency net.logstash.logback:logstash-logback-encoder to v7.2
- chore(deps): update dependency io.micronaut:micronaut-bom to v3.5.0
- chore(deps): update dependency io.micronaut:micronaut-bom to v3.5.1
- chore(deps): update plugin org.sonarqube to v3.4.0.2513
- chore(deps): update plugin org.owasp.dependencycheck to v7.1.1
- chore(deps): update docker orb to v2.1.2
- chore(deps): update dependency io.micronaut:micronaut-bom to v3.5.2
- chore(deps): update dependency io.micronaut:micronaut-bom to v3.5.3
- fix(deps): update dependency io.micronaut:micronaut-bom to v3.5.4
- fix(deps): update dependency org.objenesis:objenesis to v3.3
- fix(deps): update dependency org.apache.kafka:kafka-clients to v3
- fix(deps): update dependency io.micronaut:micronaut-bom to v3.6.1
- chore(deps): update plugin org.owasp.dependencycheck to v7.1.2
- chore(deps): update docker orb to v2.1.3
- fix(deps): update dependency org.jsoup:jsoup to v1.15.3
- feat: Updated changelog updater user
- chore(deps): update docker orb to v2.1.4
- chore(deps): update dependency gradle to v6.9.3
- fix(deps): update dependency io.micronaut:micronaut-bom to v3.7.3
- chore(deps): update plugin org.owasp.dependencycheck to v7.3.0
- chore(deps): update plugin org.sonarqube to v3.5.0.2730
- chore(deps): update plugin org.owasp.dependencycheck to v7.3.2
- fix(deps): update dependency io.micronaut:micronaut-bom to v3.7.4
- fix(deps): update dependency com.fasterxml.jackson.core:jackson-databind to v2.14.1
- fix(deps): update dependency ch.qos.logback:logback-classic to v1.4.5
- fix(deps): update dependency org.apache.kafka:kafka-clients to v3.3.1

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