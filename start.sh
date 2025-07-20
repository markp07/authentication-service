#!/usr/bin/env bash

if [ "$1" == "onlyTrainInformation" ]; then
  docker compose build --no-cache train-information
  docker compose up -d train-information
else
  docker system prune -f
  docker compose build --no-cache
  docker compose up -d
fi