# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- OpenAPI 3.0 specification for Weather Service (`weather-api-v1.yaml`)
- OpenAPI Generator Maven plugin configuration for Weather Service
- Automated code generation for Weather Service controller interfaces and models
- WeatherModelMapper for converting between domain models and API models
- Comprehensive API documentation for both Authentication and Weather services
- Passkey/WebAuthn endpoints to Authentication Service OpenAPI specification for complete API documentation

### Changed
- Refactored WeatherController to implement generated WeatherApi interface
- Updated README.md with OpenAPI documentation and code generation details
- Enhanced Weather Service with proper API specification and validation
- Enhanced Authentication Service OpenAPI spec with complete Passkey endpoint documentation

## [1.0.0] - 2025-11-02
## [1.3.3] - 2025-11-13

### Changed
- Refactor profile page: add passkey status, email verification, relocate delete action (#53)

## [1.3.2] - 2025-11-12

### Changed
- Update weather page to add popup for hourly data graphs (#54)

## [1.3.1] - 2025-11-12

### Changed
- Add Dicebear profile pictures with base64-encoded username seed (#52)

## [1.3.0] - 2025-11-11

### Changed
- Refactoring of Passkey registration and authentication (#51)

## [1.2.1] - 2025-11-11

### Changed
- Fix sidebar height overflow, add route-based navigation, update sidebar branding, and make sidebar sticky (#50)

## [1.2.0] - 2025-11-10

### Changed
- refactor: simplify Docker script for service updates and cleanup (#49)

## [1.1.0] - 2025-11-10

### Changed
- Resolve broken passkey login (#48)

## [1.0.0] - 2025-11-10

### Changed
- Add automatic semantic versioning with branch-based version bumps (#47)
- chore(docker)(deps): bump node (#42)
- chore(docker)(deps): bump eclipse-temurin (#45)
- chore(npm)(deps-dev): bump the npm-dependencies group (#43)
- chore(maven)(deps): bump the maven-dependencies group with 3 updates (#44)
- chore(docker)(deps): bump eclipse-temurin (#46)
- Update PostgreSQL volume path in docker-compose (#41)
- Optimize weather layout: scale current icon to fill height, distribute forecast columns (#40)
- Optimize weather UI spacing and typography for mobile viewports (#39)
- Add unit tests for uncovered utility, filter, service, and controller classes (#37)
- Reduce padding on mobile dashboard to maximize weather data visibility (#38)
- Redesign profile, security, user management, and modals with modern sidebar navigation (#35)
- Add OpenAPI specifications and code generation for authentication and weather services (#33)
- Fix WebAuthn userHandle encoding mismatch breaking Bitwarden and usernameless flows (#34)
- Simplify button label in login component for improved clarity
- Refactor login component to enhance user flow with distinct steps for email and password, and improve passkey login options
- Enable subdomain origin allowance in WebAuthn configuration
- Enhance WebAuthn configuration with additional security options for origin validation and user verification
- Refactor authentication flow to use custom DTO for PublicKeyCredentialRequestOptions, excluding null values
- Add JsonNullableModule to Jackson configuration for better null handling


### Added
- Dependabot configuration for automated dependency updates (Maven, npm, Docker)
- Dependabot grouping for streamlined PR management

### Changed
- Updated all dependencies to latest versions
- Refactored JWT parsing methods to use updated API for claims extraction

## [0.9.0] - 2025-10-11

### Changed
- Updated all dependency versions to latest stable releases
- Refactored JWT token parsing to use updated JJWT API

## [0.8.0] - 2025-10-08

### Changed
- Updated all dependency versions

## [0.7.0] - 2025-09-05

### Fixed
- Fixed start index calculation in WeatherMapper to correctly handle future times

## [0.6.0] - 2025-08-26

### Added
- PasskeyInfoDto for better passkey information handling

### Changed
- Refactored passkey handling in service and controller layers

## [0.5.0] - 2025-08-24

### Added
- Initial Passkey/WebAuthn login implementation
- Passkey registration and authentication endpoints

### Changed
- Refactored user interface for consistency
- Improved code consistency across multiple files
- Enhanced WindDirection enum for better degree mapping

### Fixed
- Fixed hourly forecast list starting two hours too late
- Fixed frontend build issues

## [0.4.0] - 2025-08-14

### Added
- Micrometer dependencies for metrics and monitoring
- Health probes in application configuration
- Actuator endpoints for application monitoring

### Changed
- Renamed Redis and Postgres services in docker-compose for clarity
- Updated excluded paths in application.yaml for actuator endpoints
- Enhanced logging in JwtAuthenticationFilter to include request path
- Replaced monitoring network with default-network

## [0.3.0] - 2025-08-12

### Changed
- Updated hourly forecast to display 48 hours of data
- Updated weather forecast endpoint to return 3 days of data
- Adjusted start index in WeatherMapper for accurate hourly data

## [0.2.0] - 2025-08-06

### Added
- Initial passkey/WebAuthn setup and configuration

### Fixed
- Fixed npm build issues in frontend

## [0.1.0] - 2025-08-05

### Added
- Comprehensive unit tests for services and controllers
- Test coverage for authentication flows

## [0.0.9] - 2025-08-03

### Added
- 2FA backup code generation functionality
- 2FA reset using backup code
- BackupCodesModal and SecurityPage components
- Reverse geocoding functionality for weather service

### Changed
- Refactored 2FA management components
- Refactored user profile components
- Refactored authentication API calls to use centralized fetch utility
- Updated weather component styles for improved responsiveness
- Removed unused onClose props from authentication components

### Fixed
- Handle null responses in weather service
- Updated wind direction representation

## [0.0.8] - 2025-07-31

### Added
- 2FA backup code generation endpoint
- 2FA reset functionality with backup codes

## [0.0.7] - 2025-07-30

### Added
- Autofill support for TOTP codes
- Enhanced logging throughout the application
- Restart policy for Docker services

### Changed
- Updated API endpoints across frontend and backend
- Updated CORS configuration for proper frontend integration
- Refactored authentication API endpoints (removed '/auth' prefix)

### Fixed
- Fixed URL configurations in frontend components
- Fixed security filter configuration
- Fixed various CORS-related issues

## [0.0.6] - 2025-07-28

### Added
- Account deletion functionality
- 2FA disable functionality

### Changed
- Enhanced user account management features
- Refactored username handling to use camelCase
- Improved consistency in user profile management
- Enhanced user details retrieval and update functionality

## [0.0.5] - 2025-07-27

### Added
- User profile page with sidebar navigation
- Account deletion feature

### Changed
- Enhanced Two-Factor Authentication (2FA) flow
- Improved 2FA verification and setup process
- Refactored code for better maintainability
- Improved error handling and messages
- Enhanced API specification

## [0.0.4] - 2025-07-25

### Added
- JWT authentication filter with error handling
- CORS support in security configuration

### Changed
- Enhanced security configuration with improved JWT authentication
- Enhanced logging in authentication filter

## [0.0.3] - 2025-07-24

### Added
- Two-Factor Authentication (2FA) support with TOTP
- 2FA setup, enable, disable, and verify endpoints
- QR code generation for 2FA setup
- UserController for user management
- CustomExceptionHandler for centralized exception handling
- User profile page in frontend

### Changed
- Refactored PasswordService with password strength validation
- Enhanced JwtAuthenticationFilter with improved token handling
- Updated SecurityConfig with CORS configuration

## [0.0.2] - 2025-07-23

### Added
- Comprehensive unit tests for services
- Code refactoring for improved maintainability

## [0.0.1] - 2025-07-22

### Added
- Common module with shared exceptions and handlers
- TraceparentFilter for distributed tracing support
- Weather page in frontend with real-time data
- Secure cookie configuration (conditional on profile)

### Changed
- Updated weather data handling and display

## [0.0.0] - 2025-07-21

### Added
- Initial Next.js frontend consuming authentication APIs
- Login, registration, and profile components
- Weather display integration

### Changed
- Updated APIs to work with frontend integration

## Initial Release - 2025-07-20

### Added
- Initial authentication service with Spring Boot Security
- User registration endpoint
- Login and logout functionality
- JWT-based authentication with refresh tokens
- Password change functionality
- Forgot password flow with email notifications
- Reset password functionality
- Email service with template support
- Spring Security configuration
- BCrypt password encoding
- PostgreSQL database integration
- Redis session storage
- User repository and service layer
- Initial WeatherService with MapStruct mappers
- Feign clients to Open-Meteo API
- Docker Compose configuration
- Maven parent POM with dependency management
- Basic project structure with authentication-service and weather-service
- MIT License
- Initial .gitignore configuration

### Security
- Implemented secure password storage with BCrypt
- JWT token generation and validation
- Refresh token rotation mechanism
- Session management with Redis
- CORS configuration for API security

---

## Version History Summary

- **1.0.0** - Stable release with Dependabot integration
- **0.9.0** - Dependency updates and JWT refactoring
- **0.6.0-0.8.0** - Bug fixes and dependency updates
- **0.5.0** - Passkey/WebAuthn implementation
- **0.4.0** - Monitoring and observability features
- **0.3.0** - Weather service enhancements
- **0.2.0** - Initial passkey setup
- **0.1.0** - Comprehensive test coverage
- **0.0.x** - Feature development (2FA, user management, frontend)
- **Initial** - Core authentication service and infrastructure

[1.0.0]: https://github.com/markp07/demo-authentication/compare/v0.9.0...v1.0.0
