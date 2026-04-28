# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).



## [2.0.1] - 2026-04-28

### Changed
- chore(maven)(deps): bump the maven-dependencies group across 1 directory with 8 updates (#145)
- chore(npm)(deps): bump the npm-dependencies group across 1 directory with 6 updates (#146)


## [2.0.0] - 2026-04-16

### Changed
- feat(agents): add machine-readable policy and documentation for AI agents (#142)


## [1.11.10] - 2026-04-16

### Changed
- chore(npm)(deps): bump the npm-dependencies group (#141)
- chore(maven)(deps): bump the maven-dependencies group with 4 updates (#140)


## [1.11.9] - 2026-04-09

### Changed
- chore(npm)(deps): bump the npm-dependencies group (#139)


## [1.11.8] - 2026-04-09

### Changed
- chore(maven)(deps): bump the maven-dependencies group with 2 updates (#138)
- Add CSRF token endpoint for Double Submit Cookie pattern (#137)


## [1.11.7] - 2026-04-01

### Changed
- Add ADMIN role-gated user management panel + promote-to-admin script (#136)


## [1.11.6] - 2026-04-01

### Changed
- feat: account lockout / brute force protection + Java 25 (#135)


## [1.11.5] - 2026-04-01

### Changed
- Fix NEXT_PUBLIC_EXTRA_MENU_ITEMS not propagated through Docker build pipeline (#134)
- chore(maven)(deps): bump the maven-dependencies group with 8 updates (#133)
- chore(npm)(deps): bump the npm-dependencies group (#131)
- chore(maven)(deps): bump the maven-dependencies group across 1 directory with 11 updates (#130)
- chore(npm)(deps): bump the npm-dependencies group across 1 directory with 8 updates (#129)
- chore(github-actions)(deps): bump docker/setup-buildx-action (#127)


## [1.11.4] - 2026-02-24

### Changed
- chore(npm)(deps): bump the npm-dependencies group across 1 directory with 8 updates (#123)


## [1.11.3] - 2026-02-24

### Changed
- chore(maven)(deps): bump the maven-dependencies group across 1 directory with 12 updates (#122)


## [1.11.2] - 2026-02-04

### Changed
- chore(maven)(deps): bump the maven-dependencies group with 5 updates (#116)
- chore(npm)(deps): bump the npm-dependencies group (#117)


## [1.11.1] - 2026-01-27

### Changed
- chore(maven)(deps): bump the maven-dependencies group across 1 directory with 8 updates (#115)


## [1.11.0] - 2026-01-26

### Changed
- Feature/springboot 4 migration (#114)


## [1.10.6] - 2026-01-26

### Changed
- chore(npm)(deps): bump the npm-dependencies group (#112)
- Migrate PostgreSQL from Docker volume to host directory and add Flyway (#107)


## [1.10.5] - 2026-01-25

### Changed
- chore(npm)(deps): bump the npm-dependencies group (#110)


## [1.10.4] - 2026-01-14

### Changed
- chore(npm)(deps-dev): bump the npm-dependencies group (#108)


## [1.10.3] - 2026-01-09

### Changed
- chore(npm)(deps): bump the npm-dependencies group (#106)


## [1.10.2] - 2025-12-23

### Changed
- chore(npm)(deps): bump the npm-dependencies group (#102)


## [1.10.1] - 2025-12-23

### Changed
- Fix/update readme (#103)


## [1.10.0] - 2025-12-15

### Changed
- Feature/prepare for public (#101)


## [1.9.5] - 2025-12-15

### Changed
- Upgrade maven dependencies (#99)


## [1.9.4] - 2025-12-15

### Changed
- chore(npm)(deps): bump the npm-dependencies group in /frontend with 9 updates (#97)


## [1.9.3] - 2025-12-15

### Changed
- Fix npm ci failure in build pipeline due to package.json/package-lock.json version mismatch (#98)


## [1.9.2] - 2025-12-15

### Changed
- Add missing i18n translations for authentication modals and pages (#95)


## [1.9.1] - 2025-12-14

### Changed
- Fix/fix login flow (#94)


## [1.9.0] - 2025-12-13


### Changed
- **BREAKING CHANGE:** Removed weather service completely - project now focuses solely on authentication features
- **BREAKING CHANGE:** Merged all common package classes into authentication package (nl.markpost.demo.common.* → nl.markpost.demo.authentication.*)
- Merged common module into authentication-service for better maintainability
- Updated home page to redirect to /profile instead of weather dashboard
- Removed dashboard from navigation (Profile and Security only)
- Changed default login redirect from / to /profile
- Renamed application from "Weather" to "Auth Demo" in UI
- Consolidated package structure: all shared utilities now in authentication.constant, authentication.exception, authentication.handler, authentication.model

### Removed
- Weather service module and all related endpoints
- Common module (merged into authentication-service)
- nl.markpost.demo.common package (moved to nl.markpost.demo.authentication)
- Weather-related frontend pages (dashboard, demo)
- Weather-related components (HourlyGraphModal, LocationBar, LocationEditModal, etc.)
- Weather-related types and utilities
- Weather translations and weather-specific UI elements

### Added
- Common utilities fully integrated into authentication package structure
- Refactoring documentation (REFACTORING_VERIFICATION_COMPLETE.md)


## [1.8.0] - 2025-12-11

### Changed
- feat: enable CORS for all subdomains of markpost.dev (#92)

## [1.7.1] - 2025-12-10

### Changed
- Fix/authentication callback (#91)

## [1.7.0] - 2025-12-10

### Changed
- Adding multi language support - EN NL DE FR (#90)

## [1.6.11] - 2025-12-10

### Changed
- feat: configure cookie domain settings for different environments (#89)

## [1.6.10] - 2025-12-09

### Changed
- chore(github-actions)(deps): bump the github-actions group with 3 updates (#87)

## [1.6.9] - 2025-12-08

### Changed
- chore: update GitHub Actions configuration and add weekly schedule fo… (#86)

## [1.6.8] - 2025-12-08

### Changed
- chore(npm)(deps): bump the npm-dependencies group (#84)

## [1.6.7] - 2025-12-07

### Changed
- Refactor location bar UI: uniform widths, text truncation, centralized edit modal, and unlimited locations (#82)

## [1.6.6] - 2025-12-07

### Changed
- Make sidebar version dynamic from package.json (#83)

## [1.6.5] - 2025-12-07

### Changed
- Passkey set id null before saving. Try fixing create passkey error

## [1.6.4] - 2025-12-07

### Changed
- Add retry mechanism with exponential backoff for weather API calls (#81)

## [1.6.3] - 2025-12-07

### Changed
- Optimize CI build with parallel execution and dependency caching (#80)

## [1.6.2] - 2025-12-07

### Changed
- Add token validation with exponential backoff retry and open redirect protection (#79)

## [1.6.1] - 2025-12-07

### Changed
- Add weather icons and swipeable hourly graph with fixed y-axis (#78)

## [1.6.0] - 2025-12-06

### Changed
- feat: add initial unit tests for profile picture URL generation (#77)

## [1.5.9] - 2025-12-03

### Changed
- Fix saved location deletion using locationId instead of database id (#76)

## [1.5.8] - 2025-12-02

### Changed
- Disable vertical scrolling on saved locations bar (#75)

## [1.5.7] - 2025-11-29

### Changed
- Email verification: frontend URL, from-name config, token display, manual verification UI (#73)

## [1.5.6] - 2025-11-29

### Changed
- Update date format created date on profile page
- Fix profile date format, add email verification flow, externalize email config (#71)

## [1.5.5] - 2025-11-25

### Changed
- Make 24-hour forecast modal responsive for mobile devices and fix modal alignment (#69)

## [1.5.4] - 2025-11-24

### Changed
- Add weather-themed favicon (#66)

## [1.5.3] - 2025-11-24

### Changed
- chore(npm)(deps-dev): bump @types/react (#68)

## [1.5.2] - 2025-11-19

### Changed
- Move authentication flows to dedicated pages with callback URL support (#65)

## [1.5.1] - 2025-11-19

### Changed
- Add drag-and-drop reordering for saved weather locations (#64)

## [1.5.0] - 2025-11-18

### Changed
- Migrate weather icons from Tabler to Bootstrap icons (#63)

## [1.4.4] - 2025-11-18

### Changed
- Move saved locations from localStorage to database with user linkage (#62)

## [1.4.3] - 2025-11-18

### Changed
- chore(maven)(deps): bump the maven-dependencies group with 2 updates (#60)

## [1.4.2] - 2025-11-18

### Changed
- chore(npm)(deps): bump the npm-dependencies group (#59)

## [1.4.1] - 2025-11-17

### Changed
- Merge pull request #61

## [1.4.0] - 2025-11-15

### Changed
- Fixed registration and login flows and improved location bar UI (#58)

## [1.3.6] - 2025-11-14

### Changed
- Add city search with saved locations and horizontal location switcher for weather display (#57)

## [1.3.5] - 2025-11-13

### Changed
- Fix LazyInitializationException when accessing user passkey credentials (#56)

## [1.3.4] - 2025-11-13

### Changed
- Fix Hibernate null assignment error for boolean fields in User entity (#55)

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
