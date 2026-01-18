##########################################
# 1. Build Stage
##########################################
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy entire backend folder
COPY backend ./backend

WORKDIR /app/backend

# Fix Windows CRLF
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Build directly (the JAR is referenced via system scope)
RUN ./mvnw clean package -DskipTests

##########################################
# 2. Runtime Stage
##########################################
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

RUN addgroup -S appuser && adduser -S -G appuser appuser

# Copy built JAR from builder
COPY --from=builder /app/backend/server/target/*.jar app.jar

# IMPORTANT: Copy the shared-api JAR too
COPY --from=builder /app/backend/server/src/libs/client-api.jar /app/libs/

RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

# Include the lib folder in classpath
ENTRYPOINT ["java", "-cp", "app.jar:libs/*", "org.springframework.boot.loader.launch.JarLauncher"]

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
