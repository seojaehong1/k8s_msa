# Stage 1: Build
FROM gradle:8.8-jdk17 AS builder
WORKDIR /build

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY product-service ./product-service

RUN ./gradlew :product-service:build -x test --no-daemon

# Stage 2: Runtime
FROM amazoncorretto:17
WORKDIR /app

COPY --from=builder /build/product-service/build/libs/*.jar app.jar

EXPOSE 8001

ENTRYPOINT ["java", "-jar", "app.jar"]
