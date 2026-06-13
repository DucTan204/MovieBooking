# Bước 1: Build dự án bằng JDK 17 của Eclipse Temurin
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
# Cấp quyền và chạy build
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Bước 2: Chạy ứng dụng bằng JRE (nhẹ hơn JDK, giúp tiết kiệm RAM cho Render)
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copy file jar từ bước build sang
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]