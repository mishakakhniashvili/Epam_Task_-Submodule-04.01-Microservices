# Gym CRM microservices

This project contains:

- `discovery-service` — Eureka server on port `8761`;
- `gym-crm-service` — the main CRM API on port `8080`;
- `trainer-workload-service` — the H2-backed trainer workload API on port `8081`.

## Required configuration

Set the same Base64-encoded, minimum 32-byte JWT secret for both
application services:

```powershell
$env:JWT_SECRET_BASE64="<base64-encoded-secret>"
```

For the main service, configure PostgreSQL credentials:

```powershell
$env:GYM_DB_USERNAME="postgres"
$env:GYM_DB_PASSWORD="<database-password>"
```

`GYM_DB_URL` is optional. Each Spring profile supplies a default local
database URL. Available profiles are `local`, `dev`, `stg`, and `prod`.

No production secret or database password is stored in this repository.

## Build and test

```powershell
mvn clean test
mvn package -DskipTests
```

All three packaged JARs are executable.

## Start order

1. Start Eureka:

   ```powershell
   java -jar discovery-service/target/discovery-service-1.0-SNAPSHOT.jar
   ```

2. Start the workload service:

   ```powershell
   java -jar trainer-workload-service/target/trainer-workload-service-1.0-SNAPSHOT.jar
   ```

3. Start the main service with a database profile:

   ```powershell
   java -jar gym-crm-service/target/gym-crm-spring-boot-1.0-SNAPSHOT.jar --spring.profiles.active=local
   ```

The Eureka dashboard is available at `http://localhost:8761`.

## Workload API

Update workload:

```http
POST /api/v1/workload-events
Authorization: Bearer <service JWT with workload.write scope>
X-Transaction-Id: <optional correlation ID>
```

The PDF's seven-field request is accepted. `eventId` is an optional
idempotency extension used by the main service and its retry mechanism.

Retrieve the PDF's nested trainer/year/month model:

```http
GET /api/v1/trainers/{username}/workload
GET /api/v1/trainers/{username}/workload?year=2026&month=7
Authorization: Bearer <JWT>
```

The legacy flat monthly endpoint remains available:

```http
GET /api/v1/workload-events/{username}?year=2026&month=7
```

## Delivery guarantees

Training creation and trainee deletion store workload events in the main
database transaction. A scheduled outbox dispatcher sends pending events
through Eureka with a short-lived service JWT, circuit breaker, retry,
connection timeout, read timeout, and the originating transaction ID.

The workload service processes `eventId` values idempotently and serializes
updates for the same trainer until the H2 transaction commits.
