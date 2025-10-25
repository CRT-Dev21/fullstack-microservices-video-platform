FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY ./backend/stream-service ./stream-service
RUN mvn -f ./stream-service/pom.xml clean package -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/stream-service/target/stream-service-0.0.1-SNAPSHOT.jar stream-service.jar

ENTRYPOINT ["java", "-jar", "stream-service.jar"]