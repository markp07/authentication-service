# Use OpenJDK 21 with a slim base image
FROM eclipse-temurin:25-jdk-alpine

# Set the maintainer label
LABEL maintainer="mark@markpost.nl"

# Define a volume for temporary files
VOLUME /tmp

# Expose port 12002 (matches application.yaml)
EXPOSE 12002

# Copy the pre-built JAR from the local target directory
COPY target/authentication-*.jar app.jar

# Set environment variables (add any needed for authentication-service)
ENV SPRING_PROFILES_ACTIVE=prod

# Set the entry point to run the JAR file
ENTRYPOINT ["java","-jar","/app.jar"]