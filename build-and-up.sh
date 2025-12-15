#!/bin/bash
set -e

# Define required environment variables
REQUIRED_VARS=(
    "POSTGRES_PASSWORD"
    "EMAIL_SERVICE_ENABLED"
    "SMTP_HOST"
    "SMTP_PORT"
    "SMTP_USERNAME"
    "SMTP_PASSWORD"
    "EMAIL_FROM"
    "EMAIL_FROM_NAME"
    "EMAIL_BASE_URL"
    "NEXT_PUBLIC_API_URL"
    "BASE_DOMAIN"
    "WEBAUTHN_RP_ID"
    "WEBAUTHN_ORIGIN"
    "NEXT_PUBLIC_SUPPORT_EMAIL"
)

# Check if .env file exists
if [ ! -f .env ]; then
    echo "❌ .env file not found!"
    echo ""

    # Check if .env.example exists
    if [ ! -f .env.example ]; then
        echo "❌ .env.example file not found!"
        echo "Cannot create .env file without example template."
        exit 1
    fi

    # Copy .env.example to .env
    cp .env.example .env
    echo "✅ Created .env file from .env.example"
    echo ""
    echo "⚠️  IMPORTANT: Please update the .env file with your configuration values."
    echo "After updating .env, run this script again."
    exit 0
fi

# Load .env file (only export valid variable assignments, skip comments and empty lines)
set -a
while IFS='=' read -r key value; do
    # Skip empty lines and comments
    [[ -z "$key" || "$key" =~ ^[[:space:]]*# ]] && continue
    # Only process lines that look like variable assignments
    if [[ "$key" =~ ^[A-Z_][A-Z0-9_]*$ ]]; then
        # Remove quotes if present and export
        value="${value#\"}"
        value="${value%\"}"
        value="${value#\'}"
        value="${value%\'}"
        eval export "$key=\"$value\""
    fi
done < .env
set +a

# Check if all required variables are present in .env
MISSING_VARS=()
VARS_ADDED=false

for var in "${REQUIRED_VARS[@]}"; do
    if ! grep -q "^${var}=" .env; then
        MISSING_VARS+=("$var")

        # Try to get the value from .env.example
        if [ -f .env.example ]; then
            EXAMPLE_VALUE=$(grep "^${var}=" .env.example || echo "")
            if [ -n "$EXAMPLE_VALUE" ]; then
                echo "$EXAMPLE_VALUE" >> .env
                echo "➕ Added missing variable to .env: $var"
                VARS_ADDED=true
            fi
        fi
    fi
done

# If variables were added, ask user to configure them
if [ "$VARS_ADDED" = true ]; then
    echo ""
    echo "⚠️  IMPORTANT: New variables have been added to your .env file."
    echo "Please review and configure these variables, then run this script again."
    exit 0
fi

git pull

# Build all Maven modules (parent POM)
echo "[1/3] Building all Maven modules..."
mvn clean package -DskipTests

echo "[2/3] Building Docker images..."
docker compose build --no-cache

echo "[3/3] Starting all services with docker-compose..."
docker compose up -d

echo ""
echo "✅ All services are up and running!"
echo ""
echo "📊 Service Status:"
docker compose ps