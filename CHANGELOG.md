# Changelog

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