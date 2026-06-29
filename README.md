# Gym CRM

A Spring (plain Spring Framework + Hibernate, no Spring Boot) REST API for managing gym trainees, trainers, and training sessions. Runs on an embedded Tomcat server.

## Prerequisites

- Java 21+
- Docker and Docker Compose

## Database

A `docker-compose.yml` is provided to spin up PostgreSQL:

```yaml
services:
  postgres:
    image: postgres:18
    container_name: postgres-db

    environment:
      POSTGRES_DB: gym-crm-db
      POSTGRES_USER: gymUser
      POSTGRES_PASSWORD: secret

    ports:
      - "5432:5432"

    volumes:
      - postgres_data:/var/lib/postgresql

    restart: unless-stopped

volumes:
  postgres_data:
```

This starts PostgreSQL on `localhost:5432` with:

- Database: `gym-crm-db`
- Username: `gymUser`
- Password: `secret`

Keep `hibernate.properties` values in sync with `docker-compose.yml`.

## Configuration

Connection settings are read from `src/main/resources/hibernate.properties`:

```properties
hibernate.connection.driver_class=org.postgresql.Driver
hibernate.connection.url=jdbc:postgresql://localhost:5432/gym-crm-db
hibernate.connection.username=gymUser
hibernate.connection.password=secret

hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
hibernate.show_sql=true
hibernate.format_sql=true
hibernate.hbm2ddl.auto=validate

hikari.minimumIdle=2
hikari.maximumPoolSize=10
hikari.idleTimeout=30000
hikari.connectionTimeout=10000
hikari.maxLifetime=1800000
```

Database schema is managed by Flyway migrations under `src/main/resources/db/migration`. They run automatically on startup before Hibernate validates the schema (`hbm2ddl.auto=validate`).

## Getting Started

1. Clone the repository:

   ```bash
   git clone https://github.com/Giorgi1337/gym-app.git
   cd gym-app
   ```

2. Start PostgreSQL:

   ```bash
   docker compose up -d
   ```

3. Build the application:

   ```bash
   ./gradlew build
   ```

4. Run the application:

   ```bash
   ./gradlew run
   ```

   Or run `GymApplication.main()` directly from your IDE.

The server starts on **http://localhost:8080**.

## Stopping the Database

```bash
docker compose down
```

To also remove the persisted data volume:

```bash
docker compose down -v
```

---

## API Documentation (Swagger UI)

Once the application is running, the interactive API docs are available at:

```
http://localhost:8080/swagger-ui/index.html
```

All endpoints, request/response schemas, and required headers are documented there.

---

## Testing with Postman

A ready-made Postman collection and environment are included in the repository root:

- `Gym_CRM_API_postman_collection.json`
- `Gym_CRM_Local_postman_environment.json`

### Import into Postman

1. Open Postman.
2. Click **Import** (top-left).
3. Import both files — the collection and the environment.
4. In the top-right environment selector, choose **Gym CRM Local**.

### How the collection works

The collection is designed to be run **in order**, top to bottom. Each request stores values it receives (credentials, usernames) into environment variables so the following requests can use them automatically — you do not need to copy-paste anything between requests.

| Variable | Set by |
|---|---|
| `traineeUsername` | Register Trainee |
| `traineePassword` | Register Trainee |
| `trainerUsername` | Register Trainer (Yoga) |
| `trainerPassword` | Register Trainer (Yoga) |
| `trainer2Username` | Register Trainer 2 (Boxing) |
| `trainer2Password` | Register Trainer 2 (Boxing) |

### Run order

**1. Registration**

Run all four requests in this folder first. Three registrations succeed (201); one intentionally fails with 404 (invalid specialization). After this step, `traineeUsername`, `traineePassword`, `trainerUsername`, `trainerPassword`, `trainer2Username`, and `trainer2Password` are all populated in the environment.

**2. Auth**

- **Login - Valid** — verifies the generated credentials work (200 OK).
- **Login - Invalid Password** — verifies a wrong password is rejected (401).
- **Change Password** — changes the trainee's password to `NewPassword123` and updates `traineePassword` in the environment. All subsequent trainee requests will use the new password automatically.

**3. Trainee Profile**

- **Get Trainee Profile** — retrieves the full profile (200).
- **Update Trainee Profile** — updates last name, address, and `isActive` (200).
- **Update Trainee - Missing FirstName** — validation error test (400).

**4. Trainer Profile**

- **Get Trainer Profile** — verifies specialization is `Yoga` (200).
- **Update Trainer Profile** — submits `specialization: "Boxing"` but the API ignores it; the test asserts specialization remains `Yoga` (200).

**5. Trainer-Trainee Assignment**

- **Get Unassigned Trainers (before)** — both trainers should appear (200).
- **Update Trainee's Trainers** — assigns Trainer 1 (Yoga) to the trainee (200).
- **Get Unassigned Trainers (after)** — Trainer 1 no longer appears in the list (200).
- **Get Trainer Profile** — trainee now appears in the trainer's trainees list (200).
- **Update Trainers - Empty List** — validation error (400).
- **Update Trainers - Nonexistent Trainer** — 404.

**6. Trainings**

- **Get Training Types** — returns all available training types; no authentication required (200).
- **Add Training** — schedules a training between the assigned trainer and trainee (200).
- **Get Trainee Trainings** — returns the trainee's training list; supports optional filters (`periodFrom`, `periodTo`, `trainerName`, `trainingType`) (200).
- **Get Trainer Trainings** — returns the trainer's training list; supports optional filters (`periodFrom`, `periodTo`, `traineeName`) (200).

**7. Activation / Deactivation (sections 7 & 8 in the collection)**

- **Deactivate Trainee** — sets `isActive: false` (200, no body).
- **Reactivate Trainee** — sets `isActive: true` (200, no body).
- **Deactivate Trainer** — sets `isActive: false` (200, no body).
- **Reactivate Trainer** — sets `isActive: true` (200, no body).
- **Activate - Missing isActive** — validation error (400).

**9. Delete**

- **Delete Trainee** — deletes the trainee and all associated data (200).
- **Get Deleted Trainee** — confirms the account is gone (401 or 404).

### Running the whole suite at once

To run every request in sequence and see a pass/fail summary:

1. Click the **...** menu on the **Gym CRM API** collection.
2. Select **Run collection**.
3. Make sure **Gym CRM Local** is the active environment.
4. Click **Run Gym CRM API**.

All green means everything is working end to end.

---

## Authentication

Most endpoints require two headers on every call:

```
X-Username: <username>
X-Password: <password>
```

Credentials are validated against the database on each request. The following endpoints do **not** require authentication:

- `POST /api/trainees` — register trainee
- `POST /api/trainers` — register trainer
- `GET  /api/auth/login` — login
- `GET  /api/trainings/types` — get training types

---

## API Reference

Base URL: `http://localhost:8080/api`

### Auth

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/auth/login` | No | Validate credentials |
| PUT | `/auth/password` | Header: `X-Username` | Change password |

**Login** passes credentials in the request body:
```json
{ "username": "Nika.Beridze", "password": "aZ7kP2qLmX" }
```

**Change password** passes `X-Username` as a header and old/new passwords in the body:
```json
{ "oldPassword": "aZ7kP2qLmX", "newPassword": "qW3rT8yUiO1p" }
```

### Trainees

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/trainees` | No | Register trainee |
| GET | `/trainees/{username}` | Yes | Get profile |
| PUT | `/trainees/{username}` | Yes | Update profile |
| DELETE | `/trainees/{username}` | Yes | Delete profile |
| GET | `/trainees/{username}/unassigned-trainers` | Yes | Active trainers not yet assigned |
| PUT | `/trainees/{username}/trainers` | Yes | Replace trainer list |
| PATCH | `/trainees/{username}/status` | Yes | Activate / deactivate |
| GET | `/trainees/{username}/trainings` | Yes | Trainee's training history |

### Trainers

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/trainers` | No | Register trainer |
| GET | `/trainers/{username}` | Yes | Get profile |
| PUT | `/trainers/{username}` | Yes | Update profile |
| PATCH | `/trainers/{username}/status` | Yes | Activate / deactivate |
| GET | `/trainers/{username}/trainings` | Yes | Trainer's training history |

### Trainings

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/trainers/{trainerUsername}/trainees/{traineeUsername}/trainings` | Yes | Schedule a training |
| GET | `/trainings/types` | No | List all training types |