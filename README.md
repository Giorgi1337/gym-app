# Gym App

A multi-service Spring Boot application for managing gym trainees, trainers, training sessions, and trainer workloads.

## Services

| Service | Purpose | Port |
| --- | --- | --- |
| `gym-crm-service` | Main CRM and training API | `8080` |
| `trainer-workload-service` | Trainer workload tracking | `8081` |
| `discovery-service` | Eureka service discovery | `8761` |

## Requirements

- Java 25
- Docker Desktop with Docker Compose (for the container demonstrations)

## Getting Started

Clone the repository:

```bash
git clone https://github.com/Giorgi1337/gym-app.git
cd gym-app
```

Run all remaining commands from the repository root. The project uses a single Gradle wrapper.

Create the local environment file (optional because Compose provides development defaults):

```bash
cp .env.example .env
```

On Windows PowerShell, use:

```powershell
Copy-Item .env.example .env
```

Build and test the project:

```bash
./gradlew clean build
```

Start each service in a separate terminal, in the following order:

```bash
./gradlew :discovery-service:bootRun
./gradlew :trainer-workload-service:bootRun
./gradlew :gym-crm-service:bootRun
```

Use `gradlew.bat` instead of `./gradlew` on Windows. With the default `local` profile, the CRM service starts PostgreSQL, ActiveMQ Artemis, and MongoDB from the root `docker-compose.yml`.

Training changes are published asynchronously to the `trainer.workload` queue. The workload service validates and consumes those events; messages missing required data are moved to `trainer.workload.dlq` with a `validationErrors` property.

## Service URLs

| Resource | URL |
| --- | --- |
| CRM Scalar API documentation | http://localhost:8080/scalar |
| CRM OpenAPI document | http://localhost:8080/v3/api-docs |
| Workload Scalar API documentation | http://localhost:8081/scalar |
| Workload OpenAPI document | http://localhost:8081/v3/api-docs |
| Eureka dashboard | http://localhost:8761 |
| CRM health | http://localhost:8080/actuator/health |
| CRM build and runtime information | http://localhost:8080/actuator/info |
| Workload health | http://localhost:8081/actuator/health |
| Workload build and runtime information | http://localhost:8081/actuator/info |

## API Reference

### Authentication

| Method | Path | Description |
| --- | --- | --- |
| POST | `/auth/login` | Authenticate and receive a JWT |
| POST | `/auth/logout` | Invalidate the current JWT |
| PUT | `/auth/{username}/password` | Change a password |

Protected requests require an `Authorization: Bearer <token>` header.

### Trainees

| Method | Path | Description |
| --- | --- | --- |
| POST | `/trainees` | Register a trainee |
| GET | `/trainees/{username}` | Get a profile |
| PUT | `/trainees/{username}` | Update a profile |
| DELETE | `/trainees/{username}` | Delete a profile |
| GET | `/trainees/{username}/unassigned-trainers` | List unassigned active trainers |
| PUT | `/trainees/{username}/trainers` | Replace the trainer list |
| PATCH | `/trainees/{username}/status` | Activate or deactivate |
| GET | `/trainees/{username}/trainings` | Get training history |

### Trainers

| Method | Path | Description |
| --- | --- | --- |
| POST | `/trainers` | Register a trainer |
| GET | `/trainers/{username}` | Get a profile |
| PUT | `/trainers/{username}` | Update a profile |
| PATCH | `/trainers/{username}/status` | Activate or deactivate |
| GET | `/trainers/{username}/trainings` | Get training history |

### Trainings

| Method | Path | Description |
| --- | --- | --- |
| POST | `/trainers/{trainerUsername}/trainees/{traineeUsername}/trainings` | Schedule a training |
| GET | `/trainings/types` | List training types |

### Trainer Workloads

These endpoints are provided by `trainer-workload-service` on port `8081`.

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/trainers/{username}/workload/summary` | Get the trainer's monthly workload summary |

## OpenAPI Code Generation

The CRM controller interfaces and DTOs are generated from `gym-crm-service/src/main/resources/openapi/gym-crm.yml` during compilation. Edit that specification when changing API request or response models.

## Testing

```bash
./gradlew test
```

## Stopping Local Infrastructure

```bash
docker compose down
```

## Running in Docker

The Dockerfiles are intentionally minimal runtime images. Build the Spring Boot
JARs once, then create the application images from the repository root:

```bash
./gradlew :gym-crm-service:bootJar :trainer-workload-service:bootJar :discovery-service:bootJar
docker build -f gym-crm-service/Dockerfile -t gym-main-service:local .
docker build -f trainer-workload-service/Dockerfile -t gym-report-service:local .
```

On Windows, replace `./gradlew` with `gradlew.bat`. The same JAR build command
is required before either `docker compose ... up --build` command below.

### Start the integrated services

The default Compose file creates the `gym-network` bridge network. Containers
refer to each other by service name (`postgres`, `mongodb`, `activemq`, and
`discovery`), not `localhost`.

```bash
docker compose up --build -d
docker compose ps
```

Main is at `http://localhost:8080`, report is at `http://localhost:8081`, and
the Artemis console is at `http://localhost:8161` (`admin` / `admin` by
default). Stop it with `docker compose down` (add `-v` only when you also want
to remove development database data).

For Git Bash, WSL, or Linux/macOS, the same workflow is available through one
helper script:

```bash
bash scripts/docker.sh up
bash scripts/docker.sh status
bash scripts/docker.sh logs
bash scripts/docker.sh shell main
bash scripts/docker.sh down
```

### Inspect containers and logs

Use these commands during the mentor demonstration:

```bash
docker compose exec main sh
docker compose exec report sh
docker compose logs --tail=100 main report
docker compose logs -f main report
```

The startup logs should show Flyway migrations for `main`, a MongoDB connection
for `report`, and Eureka registration for both services. After creating a
training, `report` logs `Completed workload event transaction` when it consumes
the ActiveMQ message.

### Mentor demonstration checklist (Windows PowerShell)

```powershell
.\gradlew.bat :gym-crm-service:bootJar :trainer-workload-service:bootJar :discovery-service:bootJar
docker compose up --build -d
docker compose ps
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8081/actuator/health
docker compose exec main sh
docker compose exec report sh
docker compose logs --tail=100 main report
```

Expected result: all six containers are running, PostgreSQL and MongoDB are
healthy, and both health endpoints return `"status": "UP"`. The main and
report containers disable Spring Boot's automatic Compose lifecycle with
`SPRING_DOCKER_COMPOSE_ENABLED=false`; their database, queue, and discovery
integrations are supplied explicitly through the Compose network.
