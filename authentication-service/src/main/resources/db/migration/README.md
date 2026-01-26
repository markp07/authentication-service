# Database Migrations with Flyway

This directory contains Flyway migration scripts for managing database schema changes.

## Overview

Flyway is a database migration tool that applies versioned SQL scripts to your database in a controlled, repeatable manner. It ensures that all environments (development, staging, production) have the same database schema.

## Migration File Naming Convention

Migration files must follow this naming pattern:

```
V{version}__{description}.sql
```

Examples:
- `V1__Initial_schema.sql` - Initial database schema
- `V2__Add_user_preferences.sql` - Add user preferences table
- `V3__Add_index_to_email.sql` - Add index to email column

**Important Rules:**
- Version numbers must be unique and sequential (V1, V2, V3, etc.)
- Two underscores (`__`) separate version from description
- Use descriptive names that explain what the migration does
- Once applied, migration files should NEVER be modified

## How Flyway Works

1. **Baseline**: On first run, Flyway creates a `flyway_schema_history` table to track applied migrations
2. **Migration Detection**: Flyway scans the `db/migration` directory for new SQL files
3. **Version Check**: It compares file versions against the schema history table
4. **Apply Migrations**: New migrations are applied in version order
5. **Record**: Successfully applied migrations are recorded in the schema history

## Creating a New Migration

### Step 1: Determine the Next Version Number

Check the latest migration version:
```bash
# Look at existing migration files
ls -1 authentication-service/src/main/resources/db/migration/

# Or query the database
docker exec -it authentication-postgres psql -U authentication_service -d authentication_service \
  -c "SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;"
```

### Step 2: Create the Migration File

Create a new file in `src/main/resources/db/migration/`:

```bash
# Example: Create V2 migration
touch authentication-service/src/main/resources/db/migration/V2__Add_user_profile_table.sql
```

### Step 3: Write the Migration SQL

Write idempotent SQL that can safely run multiple times:

```sql
-- Good: Uses IF NOT EXISTS
CREATE TABLE IF NOT EXISTS user_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    bio TEXT,
    avatar_url VARCHAR(500),
    CONSTRAINT fk_user_profiles_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Good: Check before adding column
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='users' AND column_name='phone_number') THEN
        ALTER TABLE users ADD COLUMN phone_number VARCHAR(20);
    END IF;
END $$;

-- Good: Index creation
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON user_profiles(user_id);
```

### Step 4: Test the Migration

**Option 1: Test Locally**
```bash
# Start the services - Flyway will run automatically
docker compose up -d

# Check migration status
docker compose logs authentication-service | grep -i flyway

# Verify in database
docker exec -it authentication-postgres psql -U authentication_service -d authentication_service \
  -c "SELECT * FROM flyway_schema_history;"
```

**Option 2: Test with Maven**
```bash
# Run the application locally (requires PostgreSQL running)
cd authentication-service
../mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Step 5: Verify the Migration

Check that:
- ✓ Migration applied without errors
- ✓ Schema changes are correct
- ✓ Application starts successfully
- ✓ Existing functionality still works
- ✓ Migration is recorded in `flyway_schema_history`

## Migration Best Practices

### DO:
✓ **Always use IF NOT EXISTS** for CREATE statements
✓ **Test migrations locally** before committing
✓ **Make migrations reversible** when possible (create a separate down migration if needed)
✓ **Keep migrations small** and focused on one change
✓ **Add comments** explaining complex changes
✓ **Create indexes** for foreign keys and frequently queried columns
✓ **Use transactions** implicitly (Flyway wraps each migration in a transaction)
✓ **Version control** all migration files

### DON'T:
✗ **Never modify** an applied migration
✗ **Don't delete** old migration files
✗ **Avoid breaking changes** that require application code changes
✗ **Don't use database-specific** syntax unless necessary
✗ **Never commit** untested migrations

## Common Migration Scenarios

### Adding a Table

```sql
CREATE TABLE IF NOT EXISTS new_table (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Adding a Column

```sql
-- For nullable columns
ALTER TABLE users ADD COLUMN IF NOT EXISTS middle_name VARCHAR(100);

-- For non-nullable columns (provide default)
ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'active';
```

### Modifying a Column

```sql
-- Change column type
ALTER TABLE users ALTER COLUMN email TYPE VARCHAR(320);

-- Add NOT NULL constraint (ensure data is clean first!)
UPDATE users SET phone_number = '' WHERE phone_number IS NULL;
ALTER TABLE users ALTER COLUMN phone_number SET NOT NULL;
```

### Adding an Index

```sql
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- Concurrent index (PostgreSQL, doesn't lock table)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_email_status ON users(email, status);
```

### Adding a Foreign Key

```sql
ALTER TABLE user_profiles 
    ADD CONSTRAINT fk_user_profiles_user_id 
    FOREIGN KEY (user_id) REFERENCES users(id) 
    ON DELETE CASCADE;
```

### Renaming a Column

```sql
ALTER TABLE users RENAME COLUMN user_name TO username;
```

### Dropping a Table (with safety check)

```sql
DROP TABLE IF EXISTS old_table CASCADE;
```

## Handling Migration Failures

If a migration fails:

1. **Check the error message** in application logs
2. **Fix the migration file** if it hasn't been applied to production
3. **If already in production**, create a new migration to fix the issue

### Repair Failed Migration (Development Only)

```bash
# Connect to database
docker exec -it authentication-postgres psql -U authentication_service -d authentication_service

# Remove failed migration from history
DELETE FROM flyway_schema_history WHERE success = false;
```

## Rolling Back Changes

Flyway doesn't support automatic rollback. To undo changes:

1. Create a new migration with the reverse changes
2. Example: If V2 added a column, create V3 to remove it

```sql
-- V3__Remove_phone_number.sql
ALTER TABLE users DROP COLUMN IF EXISTS phone_number;
```

## Database Migration in Different Environments

### Development
- Migrations run automatically on application startup
- Use `baseline-on-migrate: true` to handle existing databases

### Production
- Migrations run automatically when deploying new version
- Always backup database before deployment
- Test migrations in staging environment first

## Flyway Configuration

Current configuration (in `application.yaml`):

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true  # Handle existing databases
    baseline-version: 0        # Baseline version number
    locations: classpath:db/migration  # Migration files location
    validate-on-migrate: true  # Validate applied migrations
```

## Troubleshooting

### "Checksum mismatch" Error
**Cause**: A previously applied migration file was modified
**Solution**: Don't modify applied migrations. Create a new migration instead.

### "Schema not empty" Error
**Cause**: Database has tables but no Flyway schema history
**Solution**: `baseline-on-migrate: true` handles this automatically

### Migration Not Running
**Cause**: File naming doesn't match pattern or version already exists
**Solution**: Check file name matches `V{number}__{description}.sql`

## Useful Commands

```bash
# View migration history
docker exec -it authentication-postgres psql -U authentication_service -d authentication_service \
  -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank;"

# Check for pending migrations (compare files vs database)
docker compose logs authentication-service | grep -i "flyway"

# View all tables
docker exec -it authentication-postgres psql -U authentication_service -d authentication_service \
  -c "\dt"

# Describe a table
docker exec -it authentication-postgres psql -U authentication_service -d authentication_service \
  -c "\d users"
```

## Further Reading

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Flyway SQL Migrations](https://flywaydb.org/documentation/concepts/migrations#sql-based-migrations)
- [Flyway with Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
