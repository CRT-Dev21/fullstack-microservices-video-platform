FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY ./backend/api-gateway ./api-gateway
RUN mvn -f ./api-gateway/pom.xml clean package -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar api-gateway.jar

ENTRYPOINT ["java", "-jar", "api-gateway.jar"]