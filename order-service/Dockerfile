# Stage 1: Build
FROM gradle:8.8-jdk17 AS builder
WORKDIR /build

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY order-service ./order-service

RUN ./gradlew :order-service:build -x test --no-daemon

# Stage 2: Runtime
FROM amazoncorretto:17
WORKDIR /app

COPY --from=builder /build/order-service/build/libs/*.jar app.jar

EXPOSE 8002

ENTRYPOINT ["java", "-jar", "app.jar"]
