#!/bin/bash
# promote-to-admin.sh
# Promotes a user to the ADMIN role by inserting into the user_roles table
# inside the running authentication-postgres Docker container.

set -euo pipefail

CONTAINER="authentication-postgres"
DB_NAME="authentication_service"
DB_USER="authentication_service"

# ── Load POSTGRES_PASSWORD ──────────────────────────────────────────────────
if [ -z "${POSTGRES_PASSWORD:-}" ]; then
  if [ -f "$(dirname "$0")/.env" ]; then
    POSTGRES_PASSWORD=$(grep -E '^POSTGRES_PASSWORD=' "$(dirname "$0")/.env" | head -1 | cut -d '=' -f2-)
  fi
fi

if [ -z "${POSTGRES_PASSWORD:-}" ]; then
  echo "❌ POSTGRES_PASSWORD is not set and could not be read from .env"
  exit 1
fi

export PGPASSWORD="$POSTGRES_PASSWORD"

# ── Prompt for email ────────────────────────────────────────────────────────
echo ""
read -rp "Enter the email address of the user to promote to ADMIN: " EMAIL

if [ -z "$EMAIL" ]; then
  echo "❌ Email address cannot be empty."
  exit 1
fi

# ── Check container is running ──────────────────────────────────────────────
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER}$"; then
  echo "❌ Docker container '${CONTAINER}' is not running."
  echo "   Start the stack first with: docker compose up -d"
  exit 1
fi

# ── Look up the user ─────────────────────────────────────────────────────────
USER_ID=$(docker exec -i "$CONTAINER" \
  psql -U "$DB_USER" -d "$DB_NAME" -At \
  -c "SELECT id FROM users WHERE email = '$EMAIL';" 2>/dev/null || true)

if [ -z "$USER_ID" ]; then
  echo "❌ No user found with email: $EMAIL"
  exit 1
fi

# ── Check if already admin ───────────────────────────────────────────────────
ALREADY_ADMIN=$(docker exec -i "$CONTAINER" \
  psql -U "$DB_USER" -d "$DB_NAME" -At \
  -c "SELECT 1 FROM user_roles WHERE user_id = '$USER_ID' AND role = 'ADMIN';" 2>/dev/null || true)

if [ "$ALREADY_ADMIN" = "1" ]; then
  echo "ℹ️  User '$EMAIL' already has the ADMIN role. Nothing to do."
  exit 0
fi

# ── Grant ADMIN role ─────────────────────────────────────────────────────────
docker exec -i "$CONTAINER" \
  psql -U "$DB_USER" -d "$DB_NAME" -At \
  -c "INSERT INTO user_roles (user_id, role) VALUES ('$USER_ID', 'ADMIN');" > /dev/null

echo "✅ User '$EMAIL' has been promoted to ADMIN."
