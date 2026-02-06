FROM gradle:8.10.2-jdk21 AS builder

# Set working directory inside container
WORKDIR /app

# Copy Gradle wrapper and build files first (for caching)
COPY build.gradle ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Build the project
RUN gradle build --no-daemon

FROM eclipse-temurin:21-jre-jammy


WORKDIR /app

# Copy built jar from builder (bootJar output)
COPY --from=builder /app/build/libs/test-backend.jar app.jar

# Expose port your app listens on
EXPOSE 4000

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
