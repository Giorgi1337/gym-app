# Gym CRM

A Spring (Hibernate, no Spring Boot) application for managing Gym Trainees, Trainers, and Trainings.

## Prerequisites
- Docker and Docker Compose

A `docker-compose.yml` is provided to spin up the database:

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

Keep `hibernate.properties` values in sync with `docker-compose`.

## Configuration

Connection settings are read from `hibernate.properties`

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
Database schema is managed by Flyway migrations under `src/main/resources/db/migration`. They run automatically on startup before Hibernate validates the schema (`hibernate.hbm2ddl.auto=validate`).


## Getting started
1. Clone Repository:

   ```bash
   git clone https://github.com/Giorgi1337/gym-app.git
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

## Stopping the Database

```bash
docker compose down
```

To also remove the persisted data volume:

```bash
docker compose down -v
```
