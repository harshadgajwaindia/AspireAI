# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Run Stage ----
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Render injects PORT env variable; Spring Boot reads it automatically
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]