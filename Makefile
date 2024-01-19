docker_tag=latest

clean:
	./gradlew clean
integration-test:
	DOCKER_TAG=$(docker_tag) docker-compose -f docker-compose-integration.yml up &
	./gradlew integrationTest
	docker-compose -f docker-compose-integration.yml down
test:
	docker-compose up &
	./gradlew test --tests '*test*prometheus*metrics*' -Dtest.logs=true -x jacocoTestCoverageVerification --info
	docker-compose down
build-all:
	docker-compose up -d
	./gradlew build -Dgraalvm=true
	docker-compose down
docker-build:
	docker build -t devatherock/kafka-lag-monitor:$(docker_tag) .