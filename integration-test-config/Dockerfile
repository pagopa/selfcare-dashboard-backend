FROM maven:3-eclipse-temurin-17@sha256:0d328fa6843bb26b60cf44d69833f241ffe96218fb29fa19df7a6603863eaae7 AS builder
FROM openjdk:17-jdk@sha256:528707081fdb9562eb819128a9f85ae7fe000e2fbaeaf9f87662e7b3f38cb7d8 AS runtime
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

WORKDIR /app

COPY "/target/selc-dashboard-0.0.2-SNAPSHOT.jar" ./app.jar


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
FROM maven:3-eclipse-temurin-17@sha256:0d328fa6843bb26b60cf44d69833f241ffe96218fb29fa19df7a6603863eaae7 AS builder
FROM openjdk:17-jdk@sha256:528707081fdb9562eb819128a9f85ae7fe000e2fbaeaf9f87662e7b3f38cb7d8 AS runtime
ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

WORKDIR /app

COPY "/target/selc-dashboard-0.0.2-SNAPSHOT.jar" ./app.jar


EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
