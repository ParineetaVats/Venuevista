# VenueVista Backend

Production-grade Spring Boot 3.4.x REST backend for **VenueVista: EventPro Ticketing & Venue Management**. Replaces all localStorage frontend demo data with real server-side persistence, pessimistic concurrency locking, JWT security, server-side pricing, seat holding, and database analytics aggregates.

---

## Technical Stack
- **Language & JDK**: Java 21
- **Framework**: Spring Boot 3.4.3
- **Data Persistence**: Spring Data JPA / Hibernate 6.6, MySQL 8.x (Production) / H2 in-memory (Test Profile)
- **Security**: Spring Security 6, JJWT 0.12.6 (HMAC-SHA512), BCrypt password hashing
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Build Tool**: Apache Maven 3.9+
- **Architecture**: Domain Entities, Java Records (DTOs), Service Layer, REST Controllers, Exception Handling Advice

---

## Requirements & Prerequisites
1. **Java 21 JDK** or newer installed.
2. **Apache Maven 3.9+** installed.
3. **MySQL Server 8.0+** running locally or remotely (optional for running unit/integration tests; tests run on embedded H2).

---

## Database Setup (MySQL)
Run the following SQL statement in your MySQL client to create the database:

```sql
CREATE DATABASE venuevista CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## Environment Variables & Configuration

The application is fully environment-driven with production defaults:

| Variable Name | Default Value | Description |
|---|---|---|
| `server.port` | `8080` | Port on which the Spring Boot server listens |
| `DB_URL` | `jdbc:mysql://localhost:3306/venuevista?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | JDBC database URL |
| `DB_USERNAME` | `root` | Database user |
| `DB_PASSWORD` | `YOUR_PASSWORD` | Database password |
| `JWT_SECRET` | `venuevista_dev_secret_key_must_be_at_least_64_ascii_characters_long_for_hmac_sha_512_security!` | HMAC-SHA512 JWT signing key (min 32 bytes) |
| `JWT_EXPIRATION_MINUTES` | `1440` | JWT token validity duration (minutes) |
| `CORS_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Allowed CORS origins (comma separated) |
| `venuevista.seed.enabled` | `true` | Enables automatic database seeding on initial boot |

---

## How to Build & Run

### 1. Run Unit & Integration Tests (No MySQL Required)
Tests run against an in-memory H2 database under the `test` profile:

```bash
mvn clean test
```

### 2. Build Executable JAR
```bash
mvn clean package
```

### 3. Run Application
```bash
# Option A: Using Spring Boot Plugin
mvn spring-boot:run

# Option B: Running packaged JAR
java -jar target/venuevista-backend-0.0.1-SNAPSHOT.jar
```

### 4. Running Frontend
Navigate to the sibling `venuevista/` directory and run:
```bash
npm install
npm run dev
```

---

## Default Demo Credentials

> [!WARNING]
> **DEVELOPMENT / DEMO CREDENTIALS WARNING**: The following accounts are seeded automatically on startup when `venuevista.seed.enabled=true`. Change or remove these credentials prior to deploying to production.

- **Admin Account**:
  - **Email**: `admin@venuevista.com`
  - **Password**: `Admin@123`
  - **Role**: `ADMIN`
- **Demo User Accounts**:
  - **Email**: `aarav@example.com` / `Password`: `Test@1234`
  - **Email**: `neha@example.com` / `Password`: `Test@1234`
  - **Email**: `rohan@example.com` / `Password`: `Test@1234`

---

## Complete API Reference Table

| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register new user & return JWT token |
| `POST` | `/api/auth/login` | Public | Authenticate user & return JWT token |
| `GET` | `/api/auth/me` | Authenticated | Get current authenticated user profile |
| `POST` | `/api/auth/logout` | Authenticated | Logout user |
| `GET` | `/api/events` | Public | Filter events (`q`, `category`, `city`, `from`, `featured`, `sort`) |
| `GET` | `/api/events/featured` | Public | Get featured events |
| `GET` | `/api/events/category/{category}` | Public | Filter events by category |
| `GET` | `/api/events/search` | Public | Search events by keyword |
| `GET` | `/api/events/categories` | Public | Get list of event categories |
| `GET` | `/api/events/{id}` | Public | Get event details by ID |
| `GET` | `/api/events/{id}/seats` | Public / Opt Auth | Get seat layout & statuses for event |
| `POST` | `/api/events/{id}/holds` | Authenticated | Hold seats for 10 minutes |
| `DELETE` | `/api/events/{id}/holds` | Authenticated | Release caller seat holds |
| `GET` | `/api/venues` | Public | Filter venues (`q`, `type`, `city`) |
| `GET` | `/api/venues/{id}` | Public | Get venue details |
| `GET` | `/api/venues/{id}/events` | Public | Get events hosted at venue |
| `POST` | `/api/venues/{id}/reservations` | Authenticated | Book a venue for rental |
| `GET` | `/api/venue-reservations/my` | Authenticated | Get user venue reservations |
| `POST` | `/api/bookings/quote` | Authenticated | Calculate exact server-side pricing quote |
| `POST` | `/api/bookings` | Authenticated | Create confirmed booking with demo payment |
| `GET` | `/api/bookings/my` | Authenticated | List user bookings (newest first) |
| `GET` | `/api/bookings/{ref}` | Authenticated | Get booking details (returns 404 if owned by someone else) |
| `GET` | `/api/bookings/{ref}/tickets` | Authenticated | Get tickets for a booking |
| `PUT` | `/api/bookings/{ref}/cancel` | Authenticated | Cancel booking & release seats |
| `GET` | `/api/tickets/my` | Authenticated | List individual ticket items for user |
| `POST` | `/api/contact` | Public | Submit contact message |
| `POST` | `/api/admin/events` | `ROLE_ADMIN` | Create new event |
| `PUT` | `/api/admin/events/{id}` | `ROLE_ADMIN` | Update existing event |
| `DELETE` | `/api/admin/events/{id}` | `ROLE_ADMIN` | Delete event (409 if has confirmed bookings) |
| `POST` | `/api/admin/venues` | `ROLE_ADMIN` | Create new venue (auto-creates 60-seat layout) |
| `PUT` | `/api/admin/venues/{id}` | `ROLE_ADMIN` | Update venue details |
| `DELETE` | `/api/admin/venues/{id}` | `ROLE_ADMIN` | Delete venue (409 if has events) |
| `GET` | `/api/admin/venues/{vId}/seats` | `ROLE_ADMIN` | Get seating layout management data |
| `POST` | `/api/admin/venues/{vId}/seats` | `ROLE_ADMIN` | Add seat(s) or new row to venue |
| `PUT` | `/api/admin/seats/{seatId}` | `ROLE_ADMIN` | Update seat active status or type (409 if in use) |
| `DELETE` | `/api/admin/seats/{seatId}` | `ROLE_ADMIN` | Delete seat (soft-deactivates if historically booked) |
| `PUT` | `/api/admin/venues/{vId}/rows/{row}/type` | `ROLE_ADMIN` | Apply seat type to entire row |
| `POST` | `/api/admin/venues/{vId}/seating/reset` | `ROLE_ADMIN` | Reset seating to default layout |
| `GET` | `/api/admin/sales/summary` | `ROLE_ADMIN` | Sales summary KPIs |
| `GET` | `/api/admin/sales` | `ROLE_ADMIN` | Filterable event sales table |
| `GET` | `/api/admin/analytics/overview` | `ROLE_ADMIN` | Analytics KPIs & conversion metrics |
| `GET` | `/api/admin/analytics/revenue-over-time` | `ROLE_ADMIN` | Monthly revenue time series |
| `GET` | `/api/admin/analytics/tickets-over-time` | `ROLE_ADMIN` | Monthly ticket count time series |
| `GET` | `/api/admin/analytics/revenue-by-event` | `ROLE_ADMIN` | Revenue breakdown by event |
| `GET` | `/api/admin/analytics/revenue-by-venue` | `ROLE_ADMIN` | Revenue breakdown by venue |
| `GET` | `/api/admin/analytics/ticket-category-distribution` | `ROLE_ADMIN` | Category breakdown (VIP/Premium/Standard) |
| `GET` | `/api/admin/analytics/occupancy-by-event` | `ROLE_ADMIN` | Seat occupancy percentage per event |
| `GET` | `/api/admin/analytics/top-events` | `ROLE_ADMIN` | Top events by revenue |
| `GET` | `/api/admin/analytics/top-venues` | `ROLE_ADMIN` | Top venues by revenue |
| `GET` | `/api/admin/dashboard/stats` | `ROLE_ADMIN` | Dashboard counts & totals |

---

## Core Architecture & Business Rules

### 1. Concurrency & Double-Booking Protection
- When creating or modifying a booking/hold, the transaction executes `@Lock(PESSIMISTIC_WRITE)` on the target `Event` row (`SELECT e FROM Event e WHERE e.id = :id`) as its first statement.
- The physical database enforces a unique composite index `UNIQUE(event_id, seat_id, active_key)` on both `booking_seats` and `seat_holds`.
- When a booking is active (`CONFIRMED`), `active_key = 1`. When cancelled, `active_key = NULL`. Since database engines permit multiple NULLs in a unique index, cancelled seats are immediately released while active double-booking is physically impossible.

### 2. Pricing Engine
- `seatPrice(type) = Math.round(basePrice * multiplier)` using `RoundingMode.HALF_UP`.
  - Multipliers: `VIP`: 2.0, `Premium`: 1.5, `Standard`: 1.0.
- `subtotal = sum(seatPrice)`
- `convenienceFee = 40 * quantity`
- `tax = Math.round(subtotal * 0.05)` using `RoundingMode.HALF_UP`.
- `total = subtotal + convenienceFee + tax`.
- All prices sent by clients are ignored and computed strictly server-side.

### 3. Seat Holds
- `POST /api/events/{eventId}/holds` locks the seats for 10 minutes (`expires_at = Instant.now() + 10m`).
- A background `@Scheduled(fixedDelay = 60000)` job marks expired holds `EXPIRED` and clears `activeKey`.

### 4. Cancellation & Refunds
- `PUT /api/bookings/{ref}/cancel` marks booking `CANCELLED`, payment `REFUNDED`, and sets `booking_seats.active_key = NULL` to free seats.
- Second cancellation attempt returns `409 BOOKING_NOT_CANCELLABLE`.

---

## Troubleshooting Guide

1. **MySQL Access Denied**:
   - Ensure MySQL server is running and check your `DB_USERNAME` and `DB_PASSWORD` environment variables.

2. **Port 8080 in use**:
   - Change `server.port` in `application.properties` or pass `-Dserver.port=8081`.

3. **JWT Secret Too Short**:
   - `JwtUtils` validates secret length on startup. Ensure `JWT_SECRET` is at least 32 bytes (64 ASCII chars recommended).

4. **CORS Rejection**:
   - If your frontend runs on a port other than 5173, set `CORS_ORIGINS=http://localhost:3000` or your active origin.

5. **Unknown database `venuevista`**:
   - Run `CREATE DATABASE venuevista;` before starting the application with MySQL.
