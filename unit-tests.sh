#!/bin/sh

additional_gradle_args=$1

docker network create ci-network || true
export DOCKER_NETWORK_IP=$(docker network inspect ci-network -f '{{range .IPAM.Config}}{{.Gateway}}{{end}}')
echo "${DOCKER_NETWORK_IP}"
docker-compose up &
./gradlew test --tests '*test*prometheus*metrics*' -Dtest.logs=true -x jacocoTestCoverageVerification --info ${additional_gradle_args}
exit_code=$?

docker-compose down
exit $exit_code