#!/bin/bash

docker_tag=$1
additional_gradle_args=$2
log_files=('logback-json.xml' 'https://raw.githubusercontent.com/devatherock/ldap-search-api/master/src/main/resources/logback.xml')

set -e

for log_file in "${log_files[@]}"
do
  rm -rf logs-intg.txt
  LOGGING_CONFIG="${log_file}" DOCKER_TAG=${docker_tag} docker compose -f docker-compose-integration.yml up --wait
  docker logs -f kafka-lag-monitor-intg > logs-intg.txt &
  LOGGING_CONFIG="${log_file}" ./gradlew clean integrationTest ${additional_gradle_args}
  docker-compose -f docker-compose-integration.yml down
done
