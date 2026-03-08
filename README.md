# EMI Planner (Loan Atlas)

A production-style full-stack application to manage personal loans and forecast monthly EMI obligations with a year-based calendar view.

## Why This Project

People often manage multiple liabilities (bank loans, personal loans, credit-card EMIs, BNPL) using spreadsheets or rough calculators.  
This project solves that by providing a focused EMI visibility platform where users can:

- manage multiple loans
- see total EMI burden month-by-month
- understand exactly which loans contribute to a selected month
- plan future obligations using a 12-month calendar

This project intentionally focuses on **forecasting and visibility** from user-provided loan data, not lender-grade amortization calculations.

## Recruiter Snapshot

- Clean layered backend architecture (`controller -> service -> repository`)
- Stateless JWT authentication with Spring Security
- Strong validation + centralized exception handling
- Query/data integrity optimizations (indexes + unique constraints)
- Redis-backed caching using Spring Cache (`@Cacheable`, `@CacheEvict`)
- Dockerized runtime stack (App + MySQL + Redis)
- Integration tests with isolated test profile (H2 + cache disabled)

## Core Features

- User registration and login (phone + password)
- JWT-secured APIs
- Loan lifecycle management:
  - create
  - update
  - delete
  - close early
- User-scoped access control for loan data
- Yearly EMI calendar (`12` month summary)
- Month-wise loan contribution breakdown
- Duplicate loan prevention per user

## Project Screenshots

Add your frontend screenshots here before publishing your portfolio.

### Authentication

Login Page 
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/44a5345b-1f63-48bf-9c5c-615952126dad" />

Register Page
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/4a11e5d9-a3eb-4170-9705-a788d4006fee" />

### Dashboard / Loans

Loans Dashboard
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/356e5ac8-d85b-4e07-ac36-4c14c6364c7b" />

Add Loan Modal
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/81fb3f7d-cd24-4a03-a071-8b50587e585f" />

Loan Details
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/26dd09a5-2e3b-424e-bbbd-34fd12416aaf" />

### Calendar / Forecasting

Yearly EMI Calendar
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/b1792119-63be-4a8d-8293-d1da3a8f7603" />

Month Breakdown
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/7d8da32c-614c-477e-b2b8-ad2ff416c161" />


## In Scope vs Out of Scope

### In Scope

- EMI forecasting based on user-entered loan data
- 12-month EMI visibility
- loan contribution tracing per month

### Out of Scope (Deliberate)

- amortization schedules
- interest vs principal breakdown
- bank API integrations
- payment reminders/tracking
- credit score or budgeting modules

## Tech Stack

### Backend

- Java 17
- Spring Boot 3.5.11
- Spring Web, Spring Data JPA, Spring Security
- Bean Validation (`jakarta.validation`)
- JWT (`jjwt`)
- Redis (`spring-boot-starter-cache`, `spring-boot-starter-data-redis`)
- MySQL

### Frontend

- HTML5 + CSS3
- Vanilla JavaScript (modular ES modules)

### Dev/Test/Infra

- Maven Wrapper
- H2 (test profile)
- Docker + Docker Compose

## Backend Architecture and Implementation

### Layered Design

- `controller`: request/response handling
- `service`: business logic + authorization checks
- `repository`: persistence access via Spring Data JPA
- `dto`: input/output contracts
- `entity`: DB mapping and constraints

### Security

- Stateless security model (`SessionCreationPolicy.STATELESS`)
- JWT validated in `JwtAuthenticationFilter`
- Authenticated user UUID stored in security context
- BCrypt password hashing

### Validation and Error Handling

- Request validation with annotations like `@NotBlank`, `@NotNull`, `@Positive`, `@Size`
- Service-level business rule enforcement (ownership checks, close-date rules)
- Centralized exception mapping in `GlobalExceptionHandler`
- Consistent API error payload with timestamp, status, code, message, path

### Data Integrity and Query Design

- `users.phone_number` index
- `loans.user_id` index
- Composite unique constraint on loans:
  - `(user_id, loan_name, provider_name, start_date)`
- User-scoped queries and pagination to keep reads efficient

### Logging

- Structured service-level logs on:
  - request start
  - success path
  - warning/failure path
- Contextual identifiers included (`userId`, `loanId`) for easier debugging

## Frontend Nature and Implementation

The frontend is intentionally framework-free, lightweight, and modular.

- `api.js`: centralized request helper, token attachment, error normalization
- `auth.js`: login/register flow, auth guards, token lifecycle
- `loans.js`: loan CRUD flows, detail screen, modal workflows
- `calendar.js`: year navigation and monthly breakdown rendering
- `ui.js`: alerts, modals, navigation behavior
- `utils.js`: formatting/sanitization utilities

UI characteristics:

- responsive layout for desktop/mobile
- modal-driven interactions for speed
- clear empty/error/success states

## Caching Layer (Redis + Spring Cache)

Caching is implemented with Spring Cache abstraction and Redis as the backend.

### Cached Read Paths

- Year calendar
- Month breakdown
- Loan by ID
- Paginated user loans
- Current user profile

### Invalidation Strategy

On loan mutations (`create`, `update`, `delete`, `close`), relevant cache regions are evicted to avoid stale forecast/list/detail views.

### Configuration Highlights

- `@EnableCaching` at app boot
- Custom Redis cache manager
- JSON serialization for cache values
- Per-cache TTL tuning
- Test profile disables cache (`spring.cache.type=none`) for deterministic tests

## Containerization

The app is packaged with Docker Compose services:

1. `app` - Spring Boot service
2. `db` - MySQL 8.0
3. `redis` - Redis 7

Containerization decisions:

- isolated bridge network
- service health checks (MySQL + Redis)
- startup dependency ordering
- persistent DB volume (`db_data`)
- env-based runtime configuration

## API Overview

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`

### User

- `GET /api/users/me`

### Loans

- `POST /api/loans`
- `GET /api/loans`
- `GET /api/loans/{loanId}`
- `PUT /api/loans/{loanId}`
- `DELETE /api/loans/{loanId}`
- `PATCH /api/loans/{loanId}/close`

### Calendar

- `GET /api/calendar/{year}`
- `GET /api/calendar/{year}/{month}`

## Run Locally

### Option 1: Docker (Recommended)

```bash
docker compose up --build
```

Then open `http://localhost:8080`.

### Option 2: Local JVM Run

Set required env/properties:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATA_REDIS_HOST`
- `SPRING_DATA_REDIS_PORT`
- `JWT_SECRET`

Run:

```bash
./mvnw spring-boot:run
```

## Testing

Run:

```bash
./mvnw test
```

Test profile uses:

- H2 in-memory database
- cache disabled

This keeps tests isolated and fast.

## Project Structure

```text
src/main/java/com/emiplanner
  config/
  controller/
  dto/
  entity/
  exception/
  repository/
  security/
  service/

src/main/resources/static
  *.html
  css/styles.css
  js/*.js
```

## Potential Enhancements

- refresh-token flow and token revocation
- fine-grained user-key cache eviction (instead of broader region eviction)
- OpenAPI/Swagger documentation
- CI pipeline and deployment workflow
- observability (metrics/tracing)

