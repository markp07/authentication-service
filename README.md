# Demo Authentication

A comprehensive demonstration project for learning modern authentication and authorization patterns using Spring Boot Security, including traditional login, TOTP-based two-factor authentication, and WebAuthn passkeys.

## Project Overview

This project serves as a learning platform for implementing secure authentication mechanisms in modern web applications. It demonstrates username/password authentication, time-based one-time password (TOTP) two-factor authentication with QR code generation, passwordless authentication using FIDO2/WebAuthn standards, JWT token management with refresh token rotation, and comprehensive password management flows.

## Project Structure

```
demo-authentication/
├── authentication-service/      # Spring Boot backend
│   ├── src/main/java/nl/markpost/demo/authentication/
│   │   ├── controller/         # REST API controllers
│   │   ├── service/            # Business logic layer
│   │   ├── repository/         # JPA repositories
│   │   ├── model/              # Domain entities
│   │   ├── security/           # Security configuration
│   │   ├── filter/             # Custom filters
│   │   ├── common/             # Utilities and exceptions
│   │   └── dto/                # Data transfer objects
│   └── pom.xml
├── frontend/                    # Next.js React frontend
│   ├── src/
│   │   ├── app/                # Next.js app router pages
│   │   └── components/         # React components
│   └── package.json
└── docker-compose.yml
```

## Technologies

### Backend
- Spring Boot 3.5.6
- Spring Security
- PostgreSQL (primary database)
- Redis (session storage and caching)
- JWT (JJWT 0.13.0)
- WebAuthn (Yubico 2.7.0 for FIDO2/WebAuthn)
- OTP-Java 2.1.0 (TOTP for 2FA)
- ZXing 3.5.3 (QR code generation)
- MapStruct, Lombok
- JPA/Hibernate
- OpenAPI/Swagger

### Frontend
- Next.js 15.4.2
- React 19.1.0
- TypeScript
- Tailwind CSS 4

### Infrastructure
- Docker and Docker Compose
- Maven
- JaCoCo (code coverage)
- JUnit 5

## Security Features

- BCrypt password hashing
- RSA-signed JWT tokens with short expiration
- Refresh token rotation with Redis storage
- CSRF protection for stateless REST APIs
- Configurable CORS
- Secure headers via Spring Security
- HTTPS-ready secure cookie configuration

## Getting Started

### Prerequisites

- Java 21 or later
- Node.js 20 or later
- Maven 3.6.3 or later
- Docker and Docker Compose

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/authentication-service.git
   cd authentication-service
   ```

2. Generate RSA keys for JWT signing:
   ```bash
   ./generate-keys.sh
   ```

3. Configure environment variables:
   ```bash
   cp .env.example .env
   ```

   Edit `.env` with your configuration:
   ```bash
   POSTGRES_PASSWORD=your_secure_password
   BASE_DOMAIN=yourdomain.tld
   NEXT_PUBLIC_API_URL=https://auth.yourdomain.tld
   EMAIL_BASE_URL=https://auth.yourdomain.tld
   WEBAUTHN_RP_ID=auth.yourdomain.tld
   WEBAUTHN_ORIGIN=https://auth.yourdomain.tld
   EMAIL_SERVICE_ENABLED=false
   ```

### Running with Docker

```bash
./build-and-up.sh
```

This starts PostgreSQL (port 12004), Redis (port 12005), Authentication Service (port 12002), and Frontend (port 12006).

Access the application at `http://localhost:12006`

### Database Storage

The PostgreSQL database is stored in the `./postgres_data/` directory on your host system for easy access and backups.

**Migrating from Docker Volume**: If you're upgrading from a version that used Docker volumes, see the [Migration Guide](MIGRATION_GUIDE.md) for instructions on how to migrate your existing database.

### Running Locally

Start PostgreSQL and Redis:
```bash
docker-compose up -d postgres redis
```

Build and run backend:
```bash
./mvnw clean install
cd authentication-service
../mvnw spring-boot:run
```

Run frontend:
```bash
cd frontend
npm install
npm run dev
```

Access at `http://localhost:3000`

## API Documentation

Swagger UI available at `http://localhost:12002/swagger-ui.html`

OpenAPI specification at `http://localhost:12002/v3/api-docs`

## API Endpoints

### Authentication
- `POST /v1/auth/register` - Register new user
- `POST /v1/auth/login` - Login with credentials
- `POST /v1/auth/logout` - Logout and invalidate tokens
- `POST /v1/auth/refresh` - Refresh access token

### Two-Factor Authentication
- `POST /v1/2fa/setup` - Initialize 2FA setup (returns QR code)
- `POST /v1/2fa/enable` - Enable 2FA with TOTP code
- `POST /v1/2fa/verify` - Verify TOTP during login
- `POST /v1/2fa/disable` - Disable 2FA
- `POST /v1/2fa/backup-code` - Generate backup code
- `POST /v1/2fa/reset` - Reset 2FA with backup code

### Passkeys/WebAuthn
- `GET /v1/passkey` - List registered passkeys
- `POST /v1/passkey/register/start` - Start passkey registration
- `POST /v1/passkey/register/finish` - Complete passkey registration
- `POST /v1/passkey/login/start` - Start passkey authentication
- `POST /v1/passkey/login/finish` - Complete passkey authentication
- `DELETE /v1/passkey/{id}` - Delete passkey

### Password Management
- `POST /v1/password/change` - Change password (authenticated)
- `POST /v1/password/forgot` - Request password reset
- `POST /v1/password/reset` - Reset password with token

### User Management
- `GET /v1/user` - Get current user details
- `PUT /v1/user/username` - Update username
- `DELETE /v1/user` - Delete account

## Configuration

Configuration files:
- `authentication-service/src/main/resources/application.yaml`
- `docker-compose.yml`
- `.env` (environment variables)

For local development, defaults work out of the box. For production, configure all domain variables to match your deployment.

## Testing

Run unit tests:
```bash
./mvnw test
```

Run with coverage:
```bash
./mvnw clean verify
```

Coverage reports generated in `target/site/jacoco/index.html`

## Versioning

This project uses Semantic Versioning with automatic version management through GitHub Actions based on branch naming conventions.

## License

MIT License - see LICENSE file for details.

## Author

Mark Post - [@markp07](https://github.com/markp07)

