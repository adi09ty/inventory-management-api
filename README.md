# Inventory Management REST API

A Spring Boot backend for managing products, suppliers and stock levels, with JWT authentication and role-based access control.

## Features
- JWT login and registration (BCrypt password hashing, stateless sessions)
- Roles: `ADMIN` can create, update and delete products and suppliers; `USER` can read data and record stock movements
- Product CRUD with search, pagination and sorting
- Stock IN/OUT with an audit trail (who, when, how much); overdrawing stock is rejected
- Low-stock report (quantity at or below reorder level)
- Optimistic locking (`@Version`) so concurrent stock updates cannot silently overwrite each other
- Bean Validation and consistent RFC 7807 error responses
- Swagger UI / OpenAPI docs
- Unit and integration tests (JUnit 5, MockMvc)
- Docker and Docker Compose with MySQL

## Tech stack
Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA / Hibernate, MySQL 8.4 (H2 for quick local runs and tests), jjwt, springdoc-openapi, JUnit 5, Maven, Docker.

## Architecture
```
Client -> Controller -> Service -> Repository -> MySQL
              |            |
             DTOs     business rules (stock checks, duplicate SKU, supplier in use)
```
- `web`: REST controllers and the global exception handler
- `service`: business logic and transactions
- `repository`: Spring Data JPA interfaces
- `domain`: JPA entities (`Product`, `Supplier`, `StockMovement`, `AppUser`)
- `security` / `config`: JWT filter, security rules, OpenAPI, admin seeding
- `dto`: request/response records (entities are never returned directly)

## Quick start (no database needed, uses in-memory H2)
Requires Java 21+.
```bash
./mvnw spring-boot:run
```
On Windows use `.\mvnw.cmd spring-boot:run`.

Swagger UI: http://localhost:8080/swagger-ui.html

A default admin exists for local runs only: username `admin`, password `admin12345`.

## Run with Docker and MySQL
1. Copy the example environment file and set your own values:
   ```bash
   cp .env.example .env
   ```
   Edit `.env`: database passwords, a random `APP_JWT_SECRET` of at least 32 characters, and `APP_ADMIN_PASSWORD`. The `.env` file is git-ignored.
2. Start everything:
   ```bash
   docker compose up --build
   ```
3. Open http://localhost:8080/swagger-ui.html and log in as `admin` with the `APP_ADMIN_PASSWORD` you set.

MySQL is also exposed on `localhost:3307` (user and password from `.env`, database `inventory`) so you can browse it with MySQL Workbench.

To run the app on your machine against any MySQL, activate the `mysql` profile and set `DB_URL`, `DB_USER`, `DB_PASSWORD`, `APP_JWT_SECRET`, `APP_ADMIN_PASSWORD`.

## Example requests
```bash
# log in
curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"<your admin password>"}'

# create a product (admin token)
curl -s -X POST localhost:8080/api/products -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sku":"KB-001","name":"Keyboard","price":49.99,"quantity":20,"reorderLevel":5}'

# record a sale
curl -s -X POST localhost:8080/api/products/1/stock -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{"type":"OUT","quantity":3,"note":"order 1001"}'
```

## Endpoints
| Method | Path | Access |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | public |
| GET | `/api/products?search=&page=&size=&sort=` | logged in |
| GET | `/api/products/{id}`, `/api/products/low-stock`, `/api/products/{id}/movements` | logged in |
| POST | `/api/products/{id}/stock` | logged in |
| POST / PUT / DELETE | `/api/products`, `/api/products/{id}` | ADMIN |
| GET | `/api/suppliers`, `/api/suppliers/{id}` | logged in |
| POST / PUT / DELETE | `/api/suppliers`, `/api/suppliers/{id}` | ADMIN |

Status codes: `400` validation, `401` not logged in or bad credentials, `403` not allowed, `404` not found, `409` duplicate SKU or concurrent update, `422` insufficient stock.

## Tests
```bash
./mvnw test
```
11 tests: stock rules (unit) and the API end to end with MockMvc (auth, roles, validation, duplicates, pagination, stock movements).

## Design notes
- Business rules live in services and the `Product` entity; controllers stay thin.
- A stock change and its movement record are saved in one transaction.
- The JWT filter reloads the user's role from the database on every request, so a role change takes effect immediately.
- The `mysql` profile has no default secrets: the app refuses to start without them.
- `ddl-auto=update` is used for simplicity; a production setup would use a migration tool such as Flyway.
