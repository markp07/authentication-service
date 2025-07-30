#!/bin/bash
set -e

git pull

# Build all Maven modules (parent POM)
echo "[1/3] Building all Maven modules..."
mvn clean package

echo "[2/3] Building Docker images..."
docker compose build

echo "[3/3] Starting all services with docker-compose..."
docker compose up -d

echo "All services are up and running!"