FROM openjdk:21-jdk-slim
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests
COPY target/*.jar app.jar
EXPOSE ${PORT:-8080}
ENTRYPOINT ["java", "-jar", "app.jar"]