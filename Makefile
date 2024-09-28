docker_tag=latest

clean:
	rm -rf build
integration-test:
	./integration-tests.sh $(docker_tag) $(additional_gradle_args)
test:
	./unit-tests.sh
build-all:
	./unit-tests.sh
	./gradlew build -x test $(additional_gradle_args)
docker-build:
	./gradlew dockerBuildNative -Dnative.threads=2 -Dnative.xmx=4096m \
	    -Dnative.tag=$(docker_tag) -Dnative.arch=native -Dnative.mode=dev ${additional_gradle_args}
