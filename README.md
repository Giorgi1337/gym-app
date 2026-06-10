# Gym CRM

In-memory gym management app built with Spring Framework 7 — no Spring Boot, no database.

## Stack

Java 25 · Spring Framework 7 · Jackson 3 · Lombok · Apache Commons · Logback · Gradle 9

## How it works

- `GymFacade` is the single entry point, delegating to `TraineeService`, `TrainerService`, and `TrainingService`
- Storage is three `ConcurrentHashMap` beans (`traineeStorage`, `trainerStorage`, `trainingStorage`)
- Seed data is loaded from JSON files at startup via `StorageInitializer` (`BeanPostProcessor`)
- Usernames are auto-generated as `First.Last`, `First.Last1`, etc. — unique across both trainees and trainers
- Passwords are randomly generated (10 chars, alphanumeric)

## Getting started

```bash
git clone https://github.com/Giorgi1337/gym-app.git
cd gym-app
./gradlew build
./gradlew run
```

## Configuration

`src/main/resources/application.properties`:

```properties
storage.trainee.path=classpath:data/trainees.json
storage.trainer.path=classpath:data/trainers.json
storage.training.path=classpath:data/trainings.json
```

## Testing

```bash
./gradlew clean test jacocoTestReport
```

Report: `build/reports/jacoco/test/html/index.html`