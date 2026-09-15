FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
ARG JAR_FILE=target/eureka-server-1.0.0-SNAPSHOT.jar
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
