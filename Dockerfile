# Sử dụng hình ảnh Java 17 gọn nhẹ
FROM openjdk:17-jdk-slim AS build
WORKDIR /app
COPY . .
# Cấp quyền và chạy build bằng wrapper có sẵn trong dự án của bạn
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
# Copy file jar được build ra (Render sẽ tự tìm file .jar trong target)
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]