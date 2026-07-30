# Gym CRM

A Spring Boot REST API for managing gym trainees, trainers, and training sessions.

## Getting Started

Clone the repository

```bash
git clone https://github.com/Giorgi1337/gym-app.git
cd gym-crm-service
```

Configure environment

```bash
cp .env.example .env
```

Build the project

```bash
./gradlew clean build
```

Run the application

```bash
./gradlew bootRun
```

Spring Boot will automatically start the PostgreSQL container defined in `docker-compose.yml`.

The server starts on **http://localhost:8080**.

## API Code Generation

Controller interfaces and DTOs are generated automatically from the OpenAPI spec at
`src/main/resources/openapi/gym-crm.yml`, using the `openapi-generator` Gradle plugin.
Generation runs as part of `compileJava` — no manual step needed.

Generated sources land in `../build/generated/src/main/java` and are not committed to the
repository. To change an endpoint's request/response shape or add a new one, edit the
YAML spec; the corresponding Java interface will be regenerated on the next build.

## Stopping the Database

If you started it manually:

```bash
docker compose down
```

To also remove the persisted data volume:

```bash
docker compose down -v
```

---

## API Documentation

Once running, open the interactive API docs in your browser:

```
http://localhost:8080/scalar
```

The raw OpenAPI spec is available at:

```
http://localhost:8080/v3/api-docs
```

All endpoints, request/response schemas, and required headers are documented there.

---

## API Reference

Base URL: `http://localhost:8080/api`

### Auth

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/login` | Authenticate and receive a JWT |
| POST | `/auth/logout` | Invalidate the current JWT |
| PUT | `/auth/{username}/password` | Change password |

**Login** passes credentials in the request body:
```json
{ 
   "username": "Nika.Beridze",
   "password": "aZ7kP2qLmX"
}
```

**Change password** passes old/new passwords in the body:
```json
{ 
   "oldPassword": "aZ7kP2qLmX", 
   "newPassword": "Bober12345"
}
```

### Security

The API uses stateless JWT bearer authentication. Send the login token on protected
requests as `Authorization: Bearer <token>`. Passwords are stored with BCrypt,
failed login attempts are temporarily rate-limited, and logged-out tokens are
blacklisted until expiration. Registration, login, training types, and health/info
endpoints are public; all other endpoints require authentication.

### Trainees

| Method | Path | Description |
|--------|------|-------------|
| POST | `/trainees` | Register trainee |
| GET | `/trainees/{username}` | Get profile |
| PUT | `/trainees/{username}` | Update profile |
| DELETE | `/trainees/{username}` | Delete profile |
| GET | `/trainees/{username}/unassigned-trainers` | Active trainers not yet assigned |
| PUT | `/trainees/{username}/trainers` | Replace trainer list |
| PATCH | `/trainees/{username}/status` | Activate / deactivate |
| GET | `/trainees/{username}/trainings` | Trainee's training history |

### Trainers

| Method | Path | Description |
|--------|------|-------------|
| POST | `/trainers` | Register trainer |
| GET | `/trainers/{username}` | Get profile |
| PUT | `/trainers/{username}` | Update profile |
| PATCH | `/trainers/{username}/status` | Activate / deactivate |
| GET | `/trainers/{username}/trainings` | Trainer's training history |

### Trainings

| Method | Path | Description |
|--------|------|-------------|
| POST | `/trainers/{trainerUsername}/trainees/{traineeUsername}/trainings` | Schedule a training |
| GET | `/trainings/types` | List all training types |
