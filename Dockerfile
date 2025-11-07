# Use OpenJDK 17
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the jar file from target
COPY target/api-gateway-1.0.0.jar app.jar

# Expose the port
EXPOSE 4000

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

