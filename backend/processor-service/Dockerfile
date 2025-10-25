FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY ./backend/processor-service ./processor-service
RUN mvn -f ./processor-service/pom.xml clean package -DskipTests

FROM openjdk:17-jdk-alpine

RUN apk update && apk add ffmpeg

WORKDIR /app
COPY --from=builder /app/processor-service/target/processor-service-0.0.1-SNAPSHOT.jar processor-service.jar

ENTRYPOINT ["java", "-jar", "processor-service.jar"]