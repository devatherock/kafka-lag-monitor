FROM eclipse-temurin:17.0.8_7-jre-alpine
COPY build/libs/kafka-lag-monitor-*-all.jar kafka-lag-monitor.jar
EXPOSE 8080
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar kafka-lag-monitor.jar
