# Use OpenJDK 21 with Alpine Linux as the base image for the build stage
FROM openjdk:21-jdk AS build
WORKDIR /workspace/app

# Copy Maven wrapper and project files to the container
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Build the project
RUN ./mvnw clean package -DskipTests

# Use OpenJDK 21 with a slim base image for the final stage
FROM openjdk:21-jdk-slim

# Set the maintainer label
LABEL maintainer="mark@markpost.nl"

# Define a volume for temporary files
VOLUME /tmp

# Expose port 8080
EXPOSE 12001

# Copy the JAR file from the build stage
COPY --from=build /workspace/app/target/authentication-*.jar app.jar

# Set environment variables (add any needed for authentication-service)
ENV SPRING_PROFILES_ACTIVE=prod

# Set the entry point to run the JAR file
ENTRYPOINT ["java","-jar","/app.jar"]