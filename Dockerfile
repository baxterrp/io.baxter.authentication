# Stage 1: Build the application
FROM gradle:8.10-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon

# Stage 2: Runtime
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copy the JAR built in Stage 1
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port your app will run on
EXPOSE 8080

# Command to run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
