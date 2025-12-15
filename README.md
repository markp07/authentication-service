# Demo Authentication

A comprehensive demonstration project for learning modern authentication and authorization patterns using Spring Boot Security, including traditional login, TOTP-based two-factor authentication (2FA), and WebAuthn passkeys.

## 🎯 Project Aim

This project serves as a learning platform for implementing secure authentication and authorization mechanisms in modern web applications. It demonstrates:

- **Traditional Authentication**: Username/password-based login with secure password storage
- **Two-Factor Authentication (2FA)**: Time-based One-Time Password (TOTP) implementation with QR code generation
- **Passkeys/WebAuthn**: Modern passwordless authentication using FIDO2/WebAuthn standards
- **JWT Token Management**: Secure token-based authentication with refresh token rotation
- **Password Management**: Password reset, change password, and forgot password flows
- **Session Management**: Redis-backed session storage with logout functionality
- **Spring Boot Security**: Comprehensive security configuration and best practices

## 🏗️ Project Structure

```
demo-authentication/
├── authentication-service/   # Main authentication service (Spring Boot)
│   ├── src/
│   │   ├── main/java/nl/markpost/demo/authentication/
│   │   │   ├── controller/      # REST API controllers
│   │   │   ├── service/         # Business logic layer
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── model/           # Domain entities
│   │   │   ├── security/        # Security configuration
│   │   │   ├── filter/          # Custom filters
│   │   │   ├── common/          # Common utilities and exceptions
│   │   │   └── dto/             # Data transfer objects
│   │   └── test/                # Unit and integration tests
│   └── pom.xml
├── frontend/                 # Next.js React frontend
│   ├── src/
│   │   ├── app/             # Next.js app router pages
│   │   └── components/      # React components
│   └── package.json
└── docker-compose.yml       # Docker orchestration
```

## 🛠️ Technologies Used

### Backend (Authentication Service)
- **Spring Boot 3.5.6**: Modern Java framework
- **Spring Security**: Authentication and authorization framework
- **PostgreSQL**: Primary database for user data
- **Redis**: Session storage and caching
- **JWT (JJWT 0.13.0)**: JSON Web Token implementation
- **WebAuthn (Yubico 2.7.0)**: FIDO2/WebAuthn server library for passkey support
- **OTP-Java 2.1.0**: TOTP implementation for 2FA
- **ZXing 3.5.3**: QR code generation for 2FA setup
- **MapStruct**: Object mapping
- **Lombok**: Boilerplate reduction
- **JPA/Hibernate**: Database ORM
- **OpenAPI/Swagger**: API documentation


### Frontend
- **Next.js 15.4.2**: React framework
- **React 19.1.0**: UI library
- **TypeScript**: Type-safe JavaScript
- **Tailwind CSS 4**: Utility-first CSS framework
- **QR Code React**: QR code display for 2FA

### Infrastructure
- **Docker & Docker Compose**: Containerization
- **Maven**: Build automation
- **JaCoCo**: Code coverage
- **JUnit 5**: Testing framework

## 🚀 Getting Started

### Prerequisites

- **Java 21** or later
- **Node.js 20** or later
- **Maven 3.6.3** or later
- **Docker** and **Docker Compose**
- **PostgreSQL 16** (or use Docker)
- **Redis 7** (or use Docker)

### Environment Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/authentication-service.git
   cd authentication-service
   ```

2. **Generate RSA keys for JWT signing**
   ```bash
   ./generate-keys.sh
   ```

3. **Configure environment variables**
   Copy the example environment file and customize it:
   ```bash
   cp .env.example .env
   ```
   
   Edit the `.env` file with your configuration:
   ```bash
   # Required: Database password
   POSTGRES_PASSWORD=your_secure_password
   
   # Required: Your domain configuration
   BASE_DOMAIN=yourdomain.tld
   NEXT_PUBLIC_API_URL=https://auth.yourdomain.tld
   EMAIL_BASE_URL=https://auth.yourdomain.tld
   WEBAUTHN_RP_ID=auth.yourdomain.tld
   WEBAUTHN_ORIGIN=https://auth.yourdomain.tld
   
   # Optional: Email service configuration
   EMAIL_SERVICE_ENABLED=false
   # If EMAIL_SERVICE_ENABLED=true, configure SMTP settings
   SMTP_HOST=smtp.example.com
   SMTP_PORT=587
   SMTP_USERNAME=your_smtp_username
   SMTP_PASSWORD=your_smtp_password
   EMAIL_FROM=no-reply@example.com
   EMAIL_FROM_NAME=Authentication Service
   ```
   
   **Important**: Replace `yourdomain.tld` with your actual domain. For local development, the defaults will work out of the box.

### Running with Docker Compose

The easiest way to run the entire stack:

```bash
# Build and start all services
./build-and-up.sh

# Or manually:
docker network create default-network
docker-compose up -d
```

This will start:
- **PostgreSQL** on port `12004`
- **Redis** on port `12005`
- **Authentication Service** on port `12002`
- **Frontend** on port `12006`

Access the application at: `http://localhost:12006`

### Running Locally (Development)

#### Backend Services

1. **Start PostgreSQL and Redis**
   ```bash
   docker-compose up -d postgres redis
   ```

2. **Build the project**
   ```bash
   ./mvnw clean install
   ```

3. **Run Authentication Service**
   ```bash
   cd authentication-service
   ../mvnw spring-boot:run
   ```

#### Frontend

```bash
cd frontend
npm install
npm run dev
```

Access at: `http://localhost:3000`

## 📚 API Documentation

### OpenAPI Specifications

This project uses OpenAPI 3.0 specifications with automated code generation for both services:

**Authentication Service:**
- **Swagger UI**: `http://localhost:12002/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:12002/v3/api-docs`
- **Spec File**: `authentication-service/src/main/resources/api/authentication-api-v1.yaml`

### Code Generation

The authentication service uses the OpenAPI Generator Maven plugin to automatically generate:
- Controller interfaces with proper annotations
- Model/DTO classes with validation
- API documentation

Generated code is created during the Maven build process and placed in `target/generated-sources`.

### Key Endpoints

#### Authentication
- `POST /v1/auth/register` - Register new user
- `POST /v1/auth/login` - Login with username/password
- `POST /v1/auth/logout` - Logout and invalidate tokens
- `POST /v1/auth/refresh` - Refresh access token

#### Two-Factor Authentication
- `POST /v1/2fa/setup` - Initialize 2FA setup (returns QR code)
- `POST /v1/2fa/enable` - Enable 2FA with TOTP code
- `POST /v1/2fa/verify` - Verify TOTP during login
- `POST /v1/2fa/disable` - Disable 2FA
- `POST /v1/2fa/backup-code` - Generate backup code
- `POST /v1/2fa/reset` - Reset 2FA with backup code

#### Passkeys/WebAuthn
- `GET /v1/passkey` - List registered passkeys
- `POST /v1/passkey/register/start` - Start passkey registration
- `POST /v1/passkey/register/finish` - Complete passkey registration
- `POST /v1/passkey/login/start` - Start passkey authentication
- `POST /v1/passkey/login/finish` - Complete passkey authentication
- `DELETE /v1/passkey/{id}` - Delete passkey

#### Password Management
- `POST /v1/password/change` - Change password (authenticated)
- `POST /v1/password/forgot` - Request password reset
- `POST /v1/password/reset` - Reset password with token

#### User Management
- `GET /v1/user` - Get current user details
- `PUT /v1/user/username` - Update username
- `DELETE /v1/user` - Delete account


## 🔐 Security Features

- **Password Security**: BCrypt hashing with configurable strength
- **JWT Tokens**: RSA-signed tokens with short expiration
- **Refresh Tokens**: Secure rotation with Redis storage
- **CSRF Protection**: Configured for stateless REST APIs
- **CORS**: Configurable cross-origin resource sharing
- **Rate Limiting**: (Redis-backed, configurable)
- **Secure Headers**: Spring Security default headers
- **HTTPS Ready**: Secure cookie flags for production

## 🧪 Testing

### Run Unit Tests
```bash
./mvnw test
```

### Run with Coverage
```bash
./mvnw clean verify
```

Coverage reports are generated in `target/site/jacoco/index.html`

## 📝 Configuration

### Configuration Files

- `authentication-service/src/main/resources/application.yaml` - Service configuration
- `docker-compose.yml` - Container orchestration
- `pom.xml` - Maven dependencies and build configuration
- `.env` - Environment-specific variables (create from `.env.example`)

### Environment Variables

All configuration can be customized via environment variables in the `.env` file:

#### Database Configuration
- `POSTGRES_PASSWORD` *(required)*: PostgreSQL database password

#### Domain Configuration
- `BASE_DOMAIN` *(default: yourdomain.tld)*: Your base domain for CORS and cookie settings
- `NEXT_PUBLIC_API_URL` *(default: https://auth.yourdomain.tld)*: Frontend API URL
- `EMAIL_BASE_URL` *(default: https://auth.yourdomain.tld)*: Base URL for email links

#### WebAuthn/Passkey Configuration
- `WEBAUTHN_RP_ID` *(default: auth.yourdomain.tld)*: WebAuthn Relying Party ID (usually your auth domain)
- `WEBAUTHN_ORIGIN` *(default: https://auth.yourdomain.tld)*: WebAuthn origin URL

#### Email Service Configuration
- `EMAIL_SERVICE_ENABLED` *(default: false)*: Set to `true` to use real SMTP, `false` for dummy (logs only)
- `SMTP_HOST` *(default: smtp.example.com)*: SMTP server hostname
- `SMTP_PORT` *(default: 587)*: SMTP server port
- `SMTP_USERNAME`: SMTP authentication username
- `SMTP_PASSWORD`: SMTP authentication password
- `EMAIL_FROM` *(default: no-reply@example.com)*: Sender email address
- `EMAIL_FROM_NAME` *(default: Authentication Service)*: Sender display name

#### Development vs Production

**For local development**, you can use the defaults or leave most variables empty. The application will:
- Use `localhost` for local profile
- Disable secure cookies
- Allow CORS from `localhost:3000`

**For production**, set all domain variables to match your deployment:
```bash
BASE_DOMAIN=example.com
NEXT_PUBLIC_API_URL=https://auth.example.com
EMAIL_BASE_URL=https://auth.example.com
WEBAUTHN_RP_ID=auth.example.com
WEBAUTHN_ORIGIN=https://auth.example.com
```

## 📦 Versioning

This project uses [Semantic Versioning](https://semver.org/spec/v2.0.0.html) with automatic version management through GitHub Actions.

### Version Bump Rules

Versions are automatically incremented based on branch naming conventions when merging PRs to `master`/`main`:

- **Major version** (`X.0.0`): Branches starting with `major/*` or PRs with `[major]` tag or containing `BREAKING CHANGE`
  - Example: `major/api-redesign` → version bumps from `1.2.3` to `2.0.0`
  
- **Minor version** (`x.Y.0`): Branches starting with `feature/*` or PRs with `[feature]` tag
  - Example: `feature/add-oauth` → version bumps from `1.2.3` to `1.3.0`
  
- **Patch version** (`x.y.Z`): Branches starting with `fix/*`, `bugfix/*`, or Dependabot PRs
  - Example: `fix/login-bug` → version bumps from `1.2.3` to `1.2.4`
  - Example: Dependabot updates → version bumps from `1.2.3` to `1.2.4`

### Automatic Actions

On each merge to `master`/`main`:
1. Version is automatically bumped in `pom.xml` and `frontend/package.json`
2. `CHANGELOG.md` is updated with commit history
3. Git tag is created (e.g., `v1.2.3`)
4. GitHub Release is created with release notes

### Manual Version Override

You can also control versioning through commit messages:
- `feat:` prefix → minor version bump
- `fix:` prefix → patch version bump
- `BREAKING CHANGE` in message → major version bump

## 🤝 Contributing

This is a demonstration project for learning purposes. Feel free to fork and experiment!

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Mark Post**
- GitHub: [@markp07](https://github.com/markp07)

