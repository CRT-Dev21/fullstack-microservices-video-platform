FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY ./backend/uploader-service ./uploader-service
RUN mvn -f ./uploader-service/pom.xml clean package -DskipTests

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/uploader-service/target/uploader-service-0.0.1-SNAPSHOT.jar uploader-service.jar

ENTRYPOINT ["java", "-jar", "uploader-service.jar"]