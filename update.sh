#!/bin/bash

# Stop train-information first (but leave others running)
docker compose stop train-information

# Pull updates for everything except train-information
docker compose pull $(docker compose config --services | grep -v '^train-information$')

# Bring up the updated services (excluding train-information)
docker compose up -d $(docker compose config --services | grep -v '^train-information$')

# Prune the system
docker system prune -f

# Finally, bring back train-information (now that redis/postgres are updated)
docker compose up -d train-information
