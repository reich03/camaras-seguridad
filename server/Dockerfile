FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace/app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG JAR_FILE=/workspace/app/target/*.jar
COPY --from=build ${JAR_FILE} app.jar

# Crear directorios para almacenamiento
RUN mkdir -p /app/videos /app/frames

ENTRYPOINT ["java","-jar","/app.jar"]
