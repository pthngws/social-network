# Sử dụng Maven để build trước khi tạo image runtime
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy toàn bộ mã nguồn vào container
COPY . .

# Build ứng dụng, bỏ qua test
RUN mvn clean package -DskipTests

# Tạo image runtime
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copy file jar từ bước build vào container
COPY --from=build /app/target/*.jar app.jar

# Mở cổng chạy ứng dụng
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
