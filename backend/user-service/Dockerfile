FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY ./backend/user-service ./user-service
RUN mvn -f ./user-service/pom.xml clean package -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/user-service/target/user-service-0.0.1-SNAPSHOT.jar user-service.jar

ENTRYPOINT ["java", "-jar", "user-service.jar"]