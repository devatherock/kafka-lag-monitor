docker_tag=latest

clean:
	rm -rf build
integration-test:
	DOCKER_TAG=$(docker_tag) docker-compose -f docker-compose-integration.yml up &
	./gradlew integrationTest
	docker-compose -f docker-compose-integration.yml down
test:
	./unit-tests.sh
build-all:
	./unit-tests.sh
	./gradlew build -x test
docker-build:
	docker build -t devatherock/kafka-lag-monitor:$(docker_tag) .