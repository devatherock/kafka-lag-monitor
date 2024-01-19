FROM eclipse-temurin:17.0.9_9-jre-alpine
COPY build/libs/kafka-lag-monitor-*-all.jar kafka-lag-monitor.jar
EXPOSE 8080
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar kafka-lag-monitor.jar
