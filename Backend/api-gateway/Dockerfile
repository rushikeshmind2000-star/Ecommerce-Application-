FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
ARG JAR_FILE=target/api-gateway-1.0.0.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]
