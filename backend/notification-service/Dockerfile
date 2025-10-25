FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY ./backend/notification-service ./notification-service
RUN mvn -f ./notification-service/pom.xml clean package -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/notification-service/target/notification-service-0.0.1-SNAPSHOT.jar notification-service.jar

ENTRYPOINT ["java", "-jar", "notification-service.jar"]