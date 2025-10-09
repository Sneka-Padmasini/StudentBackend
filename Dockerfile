# Step 1: Build the application with Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy the pom.xml and download dependencies first (for better layer caching)
COPY pom.xml /app/
RUN mvn dependency:go-offline

# Copy source files
COPY src /app/src/

# Build the application
RUN mvn clean install -DskipTests

# Step 2: Create a minimal runtime image
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Expose the port
EXPOSE 8080

# Start the app
CMD ["java", "-jar", "/app/app.jar"]
