# AI Assistant

AI Assistant is a Java project built with Maven and Docker. It integrates with the OpenAI model and the NS API to provide real-time railway information in the Netherlands.

## Table of Contents

- [Project Structure](#project-structure)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Project Structure

The project is organized into multiple modules:

1. **ai-assistant-api**: This module holds the API specification and generates the API interfaces and models.
2. **ai-assistant**: This module contains the main application logic.

## Features

- **Natural Language Processing**: Understands and processes user inputs in natural language.
- **Task Automation**: Automates repetitive tasks to save time and increase efficiency.
- **Machine Learning**: Utilizes machine learning models to continuously improve performance.
- **Customizable**: Easily customizable to suit different use cases and industries.

## Docker Setup

### Dockerfile

A Dockerfile is a script that contains a series of instructions on how to build a Docker image. Each instruction in a Dockerfile creates a layer in the image, and these layers are cached to speed up the build process. Here is a brief explanation of a typical Dockerfile setup:

1. **Base Image**: Specifies the starting point for the image, usually an official image from Docker Hub.
   ```dockerfile
   FROM openjdk:11-jre-slim
   ```

2. **Maintainer**: (Optional) Specifies the author of the Dockerfile.
   ```dockerfile
   LABEL maintainer="your-email@example.com"
   ```

3. **Working Directory**: Sets the working directory inside the container.
   ```dockerfile
   WORKDIR /app
   ```

4. **Copy Files**: Copies files from the host machine to the container.
   ```dockerfile
   COPY target/your-app.jar /app/your-app.jar
   ```

5. **Run Commands**: Executes commands in the container, such as installing dependencies.
   ```dockerfile
   RUN apt-get update && apt-get install -y some-package
   ```

6. **Expose Ports**: Informs Docker that the container listens on the specified network ports at runtime.
   ```dockerfile
   EXPOSE 8080
   ```

7. **Entry Point**: Specifies the command to run within the container when it starts.
   ```dockerfile
   ENTRYPOINT ["java", "-jar", "your-app.jar"]
   ```

### Docker Compose

`docker-compose` is a tool for defining and running multi-container Docker applications. It uses a `docker-compose.yml` file to configure the application's services, networks, and volumes. Here is a brief explanation of a typical `docker-compose.yml` setup:

1. **Version**: Specifies the version of the Docker Compose file format.
   ```yaml
   version: '3.8'
   ```

2. **Services**: Defines the services (containers) that make up the application.
   ```yaml
   services:
     app:
       image: your-app-image
       build:
         context: .
         dockerfile: Dockerfile
       ports:
         - "8080:8080"
       environment:
         - ENV_VAR=value
   ```

3. **Networks**: (Optional) Defines custom networks for the services.
   ```yaml
   networks:
     app-network:
       driver: bridge
   ```

4. **Volumes**: (Optional) Defines volumes to persist data.
   ```yaml
   volumes:
     app-data:
   ```

In summary, a Dockerfile is used to build a Docker image by specifying a series of instructions, while `docker-compose` is used to manage multi-container applications by defining services, networks, and volumes in a `docker-compose.yml` file.

## Installation

To install and run the AI Assistant locally, follow these steps:

1. **Clone the repository**
    ```sh
    git clone https://github.com/mark3970/ai-assistant.git
    cd ai-assistant
    ```

2. **Build the project using Maven**
    ```sh
    mvn clean install
    ```

3. **Run Docker Compose to build and deploy the application**
    ```sh
    ./start.sh
    ```

## Usage

Once the application is running, you can interact with the AI Assistant through the `/api/v1/chat` endpoint. This is a POST endpoint that accepts a model with a `chat` field.

Example CURL command:
```sh
curl -X POST http://localhost:8080/api/v1/chat -H "Content-Type: application/json" -d '{"chat": "Hello, AI Assistant!"}'
```

Commands:



Reformat code:
```shell
mvn com.spotify.fmt:fmt-maven-plugin:format
```

Check for dependency updates:
```shell
mvn versions:display-dependency-updates
```

Check for plugin updates:
```shell
mvn versions:display-plugin-updates
```

## Changes

Please read the [CHANGELOG.md](CHANGELOG.md) for the changes to this project.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.