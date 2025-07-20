#!/bin/bash

# Navigate to your project directory
cd /home/mark/train-information

# Fetch the latest changes from the master branch
git fetch origin master

# Check if there are new changes
if git diff --quiet HEAD origin/master; then
  echo "No new changes."
else
  echo "New changes detected. Pulling changes and restarting the application."
  git pull origin master

  # Stop the train-information service
  docker compose stop train-information

  # Build and deploy the train-information service
  docker compose build train-information
  docker compose up -d train-information
fi