##########################################
# 1. Build Stage
##########################################
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy entire backend folder (so structure stays correct)
COPY backend ./backend

# Move into backend where mvnw actually lives
WORKDIR /app/backend

# Fix Windows CRLF (VERY IMPORTANT)
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Pre-download dependencies
RUN ./mvnw dependency:go-offline -B

# Build only the server module
RUN ./mvnw -pl server -am clean package -DskipTests


##########################################
# 2. Runtime Stage
##########################################
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

RUN addgroup -S appuser && adduser -S -G appuser appuser

# Copy built JAR from builder
COPY --from=builder /app/backend/server/target/*.jar app.jar

RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
