#!/bin/bash

# Pull updates only for redis and postgres
docker compose pull authentication-redis authentication-postgres

# Bring up the updated services (only redis and postgres)
docker compose up -d authentication-redis authentication-postgres

# Prune the system
docker system prune -f

