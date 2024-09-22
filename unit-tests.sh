#!/bin/sh

additional_gradle_args=$1

docker network create ci-network || true
export DOCKER_NETWORK_IP=$(docker network inspect ci-network -f '{{range .IPAM.Config}}{{.Gateway}}{{end}}')
docker compose up --wait
./gradlew test ${additional_gradle_args}
exit_code=$?

docker compose down
exit $exit_code