#!/usr/bin/env bash

docker system prune -f
docker compose build --no-cache
docker compose up -d
