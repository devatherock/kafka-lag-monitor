docker_tag=latest
docker_network_ip=localhost

clean:
	./gradlew clean
integration-test:
	DOCKER_TAG=$(docker_tag) docker-compose -f docker-compose-integration.yml up &
	./gradlew integrationTest
	docker-compose -f docker-compose-integration.yml down
test:
	docker network create ci-network || true
	$(eval docker_network_ip=$(shell docker network inspect ci-network -f '{{range .IPAM.Config}}{{.Gateway}}{{end}}'))
	DOCKER_NETWORK_IP=$(docker_network_ip) docker-compose up &
	DOCKER_NETWORK_IP=$(docker_network_ip) ./gradlew test --tests '*test*prometheus*metrics*' -Dtest.logs=true -x jacocoTestCoverageVerification --info
	docker-compose down
build-all:
	docker-compose up -d
	./gradlew build -Dgraalvm=true
	docker-compose down
docker-build:
	docker build -t devatherock/kafka-lag-monitor:$(docker_tag) .