FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY ./backend/catalog-service ./catalog-service
RUN mvn -f ./catalog-service/pom.xml clean package -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/catalog-service/target/catalog-service-0.0.1-SNAPSHOT.jar catalog-service.jar

ENTRYPOINT ["java", "-jar", "catalog-service.jar"]