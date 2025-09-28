# Optimized Dockerfile for CodePlayground Backend
# Combines multi-stage build efficiency with security best practices

# Build stage
FROM eclipse-temurin:17-jdk AS builder

# Metadata
LABEL maintainer="CodePlayground Team <team@codeplayground.com>" \
      version="1.0.0" \
      description="CodePlayground Backend Service - Optimized Build"

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and build configuration files first for better caching
COPY gradlew ./
COPY gradle gradle/
COPY build.gradle settings.gradle checkstyle.xml ./

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies (separate layer for better caching)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src/

# Build application
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Metadata for runtime stage
LABEL maintainer="CodePlayground Team <team@codeplayground.com>" \
      version="1.0.0" \
      description="CodePlayground Backend Service - Runtime"

# Install required packages for code execution (minimal set)
RUN apk add --no-cache \
    curl \
    python3 \
    nodejs \
    npm \
    && rm -rf /var/cache/apk/*

# Create application user and group for security
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

# Set working directory
WORKDIR /app

# Create code execution directory with proper permissions
RUN mkdir -p /tmp/code-playground-exec && \
    chown -R appuser:appuser /tmp/code-playground-exec

# Copy JAR from build stage
COPY --from=builder /app/build/libs/code-playground-backend.jar app.jar

# Set ownership of application files
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containerized environments
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseContainerSupport"

# Run application with optimized JVM settings
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]