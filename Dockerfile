FROM adoptopenjdk/openjdk11-openj9:jre-11.0.8_10_openj9-0.21.0-alpine
COPY build/libs/kafka-lag-monitor-*-all.jar kafka-lag-monitor.jar
EXPOSE 8080
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar kafka-lag-monitor.jar
