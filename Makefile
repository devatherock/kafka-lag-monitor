docker_tag=latest

clean:
	rm -rf build
integration-test:
	DOCKER_TAG=$(docker_tag) docker compose -f docker-compose-integration.yml up --wait
	./gradlew integrationTest
	docker-compose -f docker-compose-integration.yml down
test:
	./unit-tests.sh
build-all:
	./unit-tests.sh
	./gradlew build -x test
docker-build:
	./gradlew dockerBuildNative -Dnative.threads=2 -Dnative.xmx=4096m \
	    -Dnative.tag=$(docker_tag) -Dnative.arch=native -Dnative.mode=dev
