# Multi-stage build for production
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY . .
RUN mvn clean package -DskipTests

# Production runtime image
FROM eclipse-temurin:21-jre-alpine

# Security: Run as non-root user
RUN addgroup -S appuser && adduser -S appuser -G appuser

WORKDIR /app

# Copy application JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Create logs directory with proper permissions
RUN mkdir -p /app/logs && chmod 755 /app && chmod 755 /app/logs

# Switch to non-root user
USER appuser

# Expose port (must match server.port in application-prod.properties)
EXPOSE 8080

# JVM arguments for production (optimized for Render's containerized environment)
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+ParallelRefProcEnabled -Dfile.encoding=UTF-8 -Dspring.profiles.active=prod"

# Health check (waits 40 seconds before starting checks)
HEALTHCHECK --interval=30s --timeout=10s --start-period=45s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

# Start application with proper error handling and output
CMD ["java", "-jar", "app.jar"]
