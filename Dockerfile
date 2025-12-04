# Stage 1: Build Stage
FROM maven:3.9.0-eclipse-temurin-17 as builder

WORKDIR /app

# Copy pom.xml
COPY pom.xml .

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime Stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar ./app.jar

# Expose port
EXPOSE 8080

# Set environment variables with defaults
ENV PORT=8080 \
    SPRING_DATASOURCE_URL=jdbc:postgresql://ep-red-leaf-a1k09yb6-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require \
    SPRING_DATASOURCE_USERNAME=neondb_owner \
    SPRING_DATASOURCE_PASSWORD=npg_Xpush3x6ITwF \
    SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver \
    SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
    GEMINI_API_KEY="" \
    GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent \
    LOGGING_LEVEL_ROOT=INFO \
    LOGGING_LEVEL_COM_EXAMPLE_DEMO=DEBUG

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/schema/tables || exit 1

# Run the application
CMD ["java", "-jar", "app.jar"]
