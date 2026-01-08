#!/bin/bash
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HOST_DB_DIR="${SCRIPT_DIR}/postgres_data"

echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  PostgreSQL Database Migration Script                       ║${NC}"
echo -e "${BLUE}║  Migrate from Docker volume to host directory               ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo -e "${RED}❌ Docker is not running. Please start Docker and try again.${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓${NC} Docker is running"
}

# Function to check if volume exists
check_volume_exists() {
    local volume_name=$1
    if ! docker volume inspect "$volume_name" > /dev/null 2>&1; then
        echo -e "${RED}❌ Docker volume '${volume_name}' does not exist.${NC}"
        echo -e "${YELLOW}This could mean:${NC}"
        echo -e "  1. You haven't run the application with Docker volumes before"
        echo -e "  2. The volume name has changed"
        echo -e "  3. The volume has already been removed"
        echo ""
        echo -e "${BLUE}Available volumes:${NC}"
        docker volume ls
        return 1
    fi
    echo -e "${GREEN}✓${NC} Docker volume '${volume_name}' found"
    return 0
}

# Function to stop and remove the PostgreSQL container
stop_postgres_container() {
    echo ""
    echo -e "${BLUE}[Step 1/5]${NC} Stopping PostgreSQL container..."
    
    if docker ps -a --format '{{.Names}}' | grep -q '^authentication-postgres$'; then
        docker stop authentication-postgres > /dev/null 2>&1 || true
        docker rm authentication-postgres > /dev/null 2>&1 || true
        echo -e "${GREEN}✓${NC} PostgreSQL container stopped and removed"
    else
        echo -e "${YELLOW}⚠${NC} PostgreSQL container not found (this is OK)"
    fi
}

# Function to create host directory
create_host_directory() {
    echo ""
    echo -e "${BLUE}[Step 2/5]${NC} Creating host directory..."
    
    if [ -d "$HOST_DB_DIR" ]; then
        echo -e "${YELLOW}⚠${NC} Directory '$HOST_DB_DIR' already exists"
        echo -e "${YELLOW}⚠${NC} Existing data will be preserved as backup"
        
        # Create backup if directory has content
        if [ "$(ls -A "$HOST_DB_DIR")" ]; then
            BACKUP_DIR="${HOST_DB_DIR}_backup_$(date +%Y%m%d_%H%M%S)"
            echo -e "${BLUE}Creating backup: ${BACKUP_DIR}${NC}"
            mv "$HOST_DB_DIR" "$BACKUP_DIR"
            mkdir -p "$HOST_DB_DIR"
            echo -e "${GREEN}✓${NC} Backup created at: $BACKUP_DIR"
        fi
    else
        mkdir -p "$HOST_DB_DIR"
        echo -e "${GREEN}✓${NC} Host directory created: $HOST_DB_DIR"
    fi
}

# Function to copy data from volume to host
copy_volume_to_host() {
    local volume_name=$1
    
    echo ""
    echo -e "${BLUE}[Step 3/5]${NC} Copying data from Docker volume to host directory..."
    echo -e "${YELLOW}This may take a few minutes depending on database size...${NC}"
    
    # Use a temporary container to copy data from the volume to the host
    docker run --rm \
        -v "${volume_name}:/source" \
        -v "${HOST_DB_DIR}:/target" \
        busybox \
        sh -c "cp -a /source/. /target/" > /dev/null 2>&1
    
    echo -e "${GREEN}✓${NC} Data copied successfully"
    
    # Show size of copied data
    local size=$(du -sh "$HOST_DB_DIR" 2>/dev/null | cut -f1)
    echo -e "${BLUE}Copied data size: ${size}${NC}"
}

# Function to verify copied data
verify_data() {
    echo ""
    echo -e "${BLUE}[Step 4/5]${NC} Verifying copied data..."
    
    if [ ! -d "${HOST_DB_DIR}/data" ]; then
        echo -e "${RED}❌ PostgreSQL data directory not found!${NC}"
        return 1
    fi
    
    echo -e "${GREEN}✓${NC} PostgreSQL data directory structure verified"
    
    # List main directories
    echo -e "${BLUE}Main directories:${NC}"
    ls -la "$HOST_DB_DIR" 2>/dev/null | grep '^d' | awk '{print "  - " $9}' | grep -v '^\s*-\s*\.$' | grep -v '^\s*-\s*\.\.$'
}

# Function to set correct permissions
set_permissions() {
    echo ""
    echo -e "${BLUE}[Step 5/5]${NC} Setting correct permissions..."
    
    # PostgreSQL container runs as user 999:999 (postgres user)
    # We need to ensure the host directory has proper permissions
    sudo chown -R 999:999 "$HOST_DB_DIR" 2>/dev/null || {
        echo -e "${YELLOW}⚠${NC} Could not change ownership to postgres user (999:999)"
        echo -e "${YELLOW}⚠${NC} You may need to run: sudo chown -R 999:999 $HOST_DB_DIR"
        echo -e "${YELLOW}⚠${NC} Or run this script with sudo"
    }
    
    chmod -R 700 "$HOST_DB_DIR/data" 2>/dev/null || true
    
    echo -e "${GREEN}✓${NC} Permissions configured"
}

# Function to cleanup old volume (optional)
cleanup_old_volume() {
    local volume_name=$1
    
    echo ""
    echo -e "${YELLOW}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${YELLOW}║  Optional: Clean up old Docker volume                       ║${NC}"
    echo -e "${YELLOW}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "The old Docker volume '${volume_name}' is no longer needed."
    echo -e "You can safely remove it to free up disk space."
    echo ""
    read -p "Do you want to remove the old Docker volume? (y/N): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}Removing Docker volume...${NC}"
        docker volume rm "$volume_name" > /dev/null 2>&1 || {
            echo -e "${RED}❌ Failed to remove volume. It may be in use.${NC}"
            return 1
        }
        echo -e "${GREEN}✓${NC} Docker volume removed successfully"
    else
        echo -e "${BLUE}Keeping Docker volume. You can remove it later with:${NC}"
        echo -e "  docker volume rm $volume_name"
    fi
}

# Main execution
main() {
    local VOLUME_NAME="authentication-service_postgres_data"
    
    # Check if running with project prefix
    if ! check_volume_exists "$VOLUME_NAME"; then
        # Try without prefix (older Docker Compose versions)
        VOLUME_NAME="postgres_data"
        if ! check_volume_exists "$VOLUME_NAME"; then
            echo ""
            echo -e "${RED}Cannot find the PostgreSQL Docker volume.${NC}"
            echo -e "${YELLOW}If you want to start fresh with a host-based database:${NC}"
            echo -e "  1. Simply run: docker compose up -d"
            echo -e "  2. A new database will be created in ./postgres_data/"
            exit 1
        fi
    fi
    
    echo ""
    echo -e "${YELLOW}⚠  WARNING: This script will:${NC}"
    echo -e "  1. Stop and remove the authentication-postgres container"
    echo -e "  2. Copy data from Docker volume '${VOLUME_NAME}' to host directory"
    echo -e "  3. Configure docker-compose.yml to use the host directory"
    echo ""
    echo -e "Make sure you have:"
    echo -e "  - Already updated docker-compose.yml to use host directory"
    echo -e "  - A backup of your data (optional but recommended)"
    echo ""
    read -p "Do you want to continue? (y/N): " -n 1 -r
    echo ""
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${YELLOW}Migration cancelled.${NC}"
        exit 0
    fi
    
    # Execute migration steps
    check_docker
    stop_postgres_container
    create_host_directory
    copy_volume_to_host "$VOLUME_NAME"
    verify_data
    set_permissions
    
    echo ""
    echo -e "${GREEN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  Migration completed successfully! ✓                        ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${BLUE}Next steps:${NC}"
    echo -e "  1. Verify docker-compose.yml uses: ${GREEN}./postgres_data:/var/lib/postgresql${NC}"
    echo -e "  2. Start your services: ${GREEN}docker compose up -d${NC}"
    echo -e "  3. Verify the database is working correctly"
    echo -e "  4. If everything works, you can remove the old Docker volume"
    echo ""
    
    # Optional cleanup
    cleanup_old_volume "$VOLUME_NAME"
    
    echo ""
    echo -e "${BLUE}Database location: ${HOST_DB_DIR}${NC}"
    echo -e "${GREEN}✓${NC} Migration complete!"
}

# Run main function
main
