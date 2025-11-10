#!/bin/bash

# Pull updates only for redis and postgres
docker compose pull redis postgres

# Bring up the updated services (only redis and postgres)
docker compose up -d redis postgres

# Prune the system
docker system prune -f

