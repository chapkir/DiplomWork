# Stage 1: build the application
FROM gradle:7.6.2-jdk17 AS builder
WORKDIR /home/gradle/project
COPY . .
# Build only the server module, skip tests for speed
RUN ./gradlew server:clean server:build -x test --no-daemon

# Stage 2: run the application
FROM openjdk:17-jdk-slim
VOLUME /tmp
# Copy the built jar from the builder stage
COPY --from=builder /home/gradle/project/server/build/libs/*.jar /app/app.jar
# Expose application port (matches server.port in application.properties)
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]