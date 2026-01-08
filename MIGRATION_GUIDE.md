# Database Migration Guide: Docker Volume to Host Directory

## Overview

This guide explains how to migrate your PostgreSQL database from a Docker volume to a directory on your host system. This change provides better visibility, easier backups, and simpler database management.

## Why Migrate to Host Directory?

### Benefits:
- **Easy Backups**: Direct access to database files for simple backup solutions
- **Better Visibility**: See exactly where your data is stored
- **Simplified Management**: Use standard file system tools to manage database files
- **Portability**: Easier to move database to different systems
- **No Volume Confusion**: Avoid Docker volume naming issues and cleanup

### Trade-offs:
- **Disk Space**: Database files visible in project directory (can be large)
- **Permissions**: Need to manage file permissions correctly (handled by script)

## Prerequisites

- Docker and Docker Compose installed and running
- Sufficient disk space for database copy
- Backup of your data (recommended but optional)
- Sudo access (for permission changes)

## Migration Methods

You can choose between two migration methods:

### Method 1: Automated Migration (Recommended)

Use the provided migration script for existing databases.

### Method 2: Fresh Start

Start with a clean database in the host directory.

---

## Method 1: Automated Migration (Recommended)

Use this method if you have an existing database in a Docker volume that you want to preserve.

### Step 1: Prepare for Migration

1. **Stop all services** (optional but recommended):
   ```bash
   docker compose down
   ```

2. **Verify you have the latest code**:
   ```bash
   git pull
   ```

3. **Check your Docker volumes**:
   ```bash
   docker volume ls | grep postgres
   ```
   
   You should see a volume named `authentication-service_postgres_data` or `postgres_data`.

### Step 2: Run the Migration Script

The repository includes an automated migration script that handles everything:

```bash
./migrate-db-to-host.sh
```

The script will:
- ✓ Check if Docker is running
- ✓ Find your PostgreSQL Docker volume
- ✓ Stop the PostgreSQL container
- ✓ Create the host directory (`./postgres_data/`)
- ✓ Copy all data from the Docker volume to the host directory
- ✓ Set correct permissions (999:999 for PostgreSQL)
- ✓ Verify the copied data
- ✓ Optionally remove the old Docker volume

### Step 3: Verify the Migration

1. **Check that the host directory was created**:
   ```bash
   ls -la postgres_data/
   ```
   
   You should see PostgreSQL data directories (`data/`, etc.).

2. **Verify docker-compose.yml was updated**:
   ```bash
   grep -A 2 "postgres_data" docker-compose.yml
   ```
   
   You should see:
   ```yaml
   volumes:
     - ./postgres_data:/var/lib/postgresql
   ```

### Step 4: Start Services

Start your services with the new configuration:

```bash
docker compose up -d
```

Or use the build script:

```bash
./build-and-up.sh
```

### Step 5: Verify Database is Working

1. **Check service health**:
   ```bash
   docker compose ps
   ```
   
   All services should show as "healthy" or "running".

2. **Check PostgreSQL logs**:
   ```bash
   docker compose logs authentication-postgres
   ```
   
   Look for successful startup messages, no errors about data corruption.

3. **Test the application**:
   - Access the frontend at `http://localhost:12006`
   - Try logging in with existing credentials
   - Verify your data is intact

### Step 6: Cleanup (Optional)

If everything works correctly, you can remove the old Docker volume:

```bash
# List volumes to confirm the old one exists
docker volume ls

# Remove the old volume (adjust name if different)
docker volume rm authentication-service_postgres_data
```

Or the script will offer to do this for you at the end.

---

## Method 2: Fresh Start

Use this method if you want to start with a clean database or don't have existing data to preserve.

### Step 1: Stop and Remove Existing Services

```bash
docker compose down -v
```

The `-v` flag removes all volumes including the database.

⚠️ **WARNING**: This will delete all existing data!

### Step 2: Update Configuration

The `docker-compose.yml` should already be updated. Verify it contains:

```yaml
services:
  authentication-postgres:
    volumes:
      - ./postgres_data:/var/lib/postgresql
```

### Step 3: Start Services

```bash
docker compose up -d
```

Or:

```bash
./build-and-up.sh
```

Docker will automatically create the `postgres_data/` directory and initialize a fresh database.

### Step 4: Initialize Your Data

You'll need to set up your application from scratch:
- Create new user accounts
- Configure settings
- Set up 2FA, passkeys, etc.

---

## Troubleshooting

### Issue: Permission Denied Errors

**Symptoms**: PostgreSQL container fails to start with permission errors.

**Solution**:
```bash
# Fix permissions (PostgreSQL runs as user 999)
sudo chown -R 999:999 postgres_data/
chmod -R 700 postgres_data/data/
```

### Issue: Old Volume Not Found

**Symptoms**: Migration script can't find `authentication-service_postgres_data` volume.

**Solution**:
```bash
# List all volumes
docker volume ls

# If you see a different name, note it and either:
# 1. Edit the migration script to use the correct volume name, or
# 2. Use Method 2 (Fresh Start)
```

### Issue: Container Won't Start After Migration

**Symptoms**: PostgreSQL container crashes or restarts constantly.

**Solution**:
1. Check logs:
   ```bash
   docker compose logs authentication-postgres
   ```

2. Verify data integrity:
   ```bash
   ls -la postgres_data/data/
   ```

3. Try rolling back:
   ```bash
   # Remove the host directory
   rm -rf postgres_data/
   
   # Restore docker-compose.yml to use Docker volume
   # (revert the volume changes)
   
   # Start services
   docker compose up -d
   ```

### Issue: Database Files are Huge

**Symptoms**: The `postgres_data/` directory takes up a lot of disk space.

**Solution**: This is normal. PostgreSQL database files can be large. Consider:
- Regular database maintenance (VACUUM)
- Archiving old data
- Setting up automated backups to external storage

### Issue: Can't Write to Host Directory

**Symptoms**: PostgreSQL logs show write errors.

**Solution**:
```bash
# Check disk space
df -h

# Verify directory exists and has correct ownership
ls -ld postgres_data/
sudo chown -R 999:999 postgres_data/
```

---

## Post-Migration Checklist

After successful migration, verify:

- [ ] All services are running (`docker compose ps`)
- [ ] PostgreSQL logs show no errors
- [ ] Application frontend is accessible
- [ ] You can log in with existing credentials
- [ ] All your data is intact (users, 2FA settings, passkeys)
- [ ] Database writes work (create test account, logout, login)
- [ ] `postgres_data/` directory exists in project root
- [ ] Old Docker volume has been removed (optional)

---

## Backup Recommendations

Now that your database is on the host filesystem, you can easily back it up:

### Simple Backup (Filesystem Copy)

```bash
# Stop PostgreSQL (important!)
docker compose stop authentication-postgres

# Create backup
tar -czf postgres_backup_$(date +%Y%m%d).tar.gz postgres_data/

# Restart PostgreSQL
docker compose start authentication-postgres
```

### Database Dump (Recommended)

```bash
# Create SQL dump (while database is running)
docker exec authentication-postgres pg_dump -U authentication_service authentication_service > backup_$(date +%Y%m%d).sql

# Or with compression
docker exec authentication-postgres pg_dump -U authentication_service authentication_service | gzip > backup_$(date +%Y%m%d).sql.gz
```

### Restore from Backup

```bash
# From SQL dump
docker exec -i authentication-postgres psql -U authentication_service authentication_service < backup.sql

# Or from compressed dump
gunzip -c backup.sql.gz | docker exec -i authentication-postgres psql -U authentication_service authentication_service
```

### Automated Backups

Consider setting up a cron job for regular backups:

```bash
# Edit crontab
crontab -e

# Add daily backup at 2 AM (adjust path as needed)
0 2 * * * cd /path/to/authentication-service && docker exec authentication-postgres pg_dump -U authentication_service authentication_service | gzip > backups/backup_$(date +\%Y\%m\%d).sql.gz
```

---

## Reverting to Docker Volume

If you need to revert to using Docker volumes:

1. **Stop services**:
   ```bash
   docker compose down
   ```

2. **Update docker-compose.yml**:
   ```yaml
   services:
     authentication-postgres:
       volumes:
         - postgres_data:/var/lib/postgresql
   
   volumes:
     postgres_data:
       driver: local
   ```

3. **Optionally copy data back to volume**:
   ```bash
   docker volume create postgres_data
   docker run --rm -v ./postgres_data:/source -v postgres_data:/target busybox sh -c "cp -a /source/. /target/"
   ```

4. **Start services**:
   ```bash
   docker compose up -d
   ```

---

## Support

If you encounter issues not covered in this guide:

1. Check the application logs: `docker compose logs`
2. Check PostgreSQL-specific logs: `docker compose logs authentication-postgres`
3. Review Docker Compose status: `docker compose ps`
4. Open an issue on GitHub with relevant log output

---

## Summary

You've successfully migrated your PostgreSQL database from a Docker volume to a host directory! Your database files are now located in `./postgres_data/` and you can:

- ✓ Easily back up your database using standard tools
- ✓ See exactly where your data is stored
- ✓ Manage database files directly if needed
- ✓ Move or copy your database more easily

Remember to:
- Keep the `postgres_data/` directory in `.gitignore` (already configured)
- Set up regular backups
- Monitor disk space usage
- Keep file permissions correct (999:999)
