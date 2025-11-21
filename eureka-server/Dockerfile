FROM amazoncorretto:17
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]