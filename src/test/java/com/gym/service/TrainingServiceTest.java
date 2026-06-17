package com.gym.service;

import com.gym.dao.TraineeDao;
import com.gym.dao.TrainerDao;
import com.gym.dao.TrainingDao;
import com.gym.dao.TrainingTypeDao;
import com.gym.model.*;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrainingServiceTest {

    private TrainingService trainingService;
    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;

    @BeforeEach
    void setup() {
        trainingDao = mock(TrainingDao.class);
        traineeDao = mock(TraineeDao.class);
        trainerDao = mock(TrainerDao.class);
        trainingTypeDao = mock(TrainingTypeDao.class);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        trainingService = new TrainingService(trainingDao, traineeDao, trainerDao, trainingTypeDao, validator);
    }

    @Test
    void addTrainingSucceeds() {
        when(traineeDao.findByUserName("John.Doe")).thenReturn(Optional.of(buildTrainee("John.Doe")));
        when(trainerDao.findByUserName("Nika.Doe")).thenReturn(Optional.of(buildTrainer("Nika.Doe")));
        when(trainingTypeDao.findByName("Yoga")).thenReturn(buildTrainingType("Yoga"));

        trainingService.addTraining("John.Doe", "Nika.Doe", "Morning Yoga", "Yoga", LocalDate.now(), 60);

        verify(trainingDao).save(any(Training.class));
    }

    @Test
    void addTrainingThrowsWhenTraineeNotFound() {
        when(traineeDao.findByUserName("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.addTraining(
                "unknown", "Nika.Doe", "Morning Yoga", "Yoga", LocalDate.now(), 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Trainee not found: unknown");
    }

    @Test
    void addTrainingThrowsWhenTrainerNotFound() {
        when(traineeDao.findByUserName("John.Doe")).thenReturn(Optional.of(buildTrainee("John.Doe")));
        when(trainerDao.findByUserName("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingService.addTraining(
                "John.Doe", "unknown", "Morning Yoga", "Yoga", LocalDate.now(), 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Trainer not found: unknown");
    }

    @Test
    void addTrainingThrowsWhenTrainingTypeNotFound() {
        when(traineeDao.findByUserName("John.Doe")).thenReturn(Optional.of(buildTrainee("John.Doe")));
        when(trainerDao.findByUserName("Nika.Doe")).thenReturn(Optional.of(buildTrainer("Nika.Doe")));
        when(trainingTypeDao.findByName("Unknown")).thenReturn(null);

        assertThatThrownBy(() -> trainingService.addTraining(
                "John.Doe", "Nika.Doe", "Morning Yoga", "Unknown", LocalDate.now(), 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Training type not found: Unknown");
    }

    @Test
    void addTrainingThrowsWhenDateIsNull() {
        when(traineeDao.findByUserName("John.Doe")).thenReturn(Optional.of(buildTrainee("John.Doe")));
        when(trainerDao.findByUserName("Nika.Doe")).thenReturn(Optional.of(buildTrainer("Nika.Doe")));
        when(trainingTypeDao.findByName("Yoga")).thenReturn(buildTrainingType("Yoga"));

        assertThatThrownBy(() -> trainingService.addTraining(
                "John.Doe", "Nika.Doe", "Morning Yoga", "Yoga", null, 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Training date is required");
    }

    @Test
    void addTrainingThrowsWhenNameIsBlank() {
        when(traineeDao.findByUserName("John.Doe")).thenReturn(Optional.of(buildTrainee("John.Doe")));
        when(trainerDao.findByUserName("Nika.Doe")).thenReturn(Optional.of(buildTrainer("Nika.Doe")));
        when(trainingTypeDao.findByName("Yoga")).thenReturn(buildTrainingType("Yoga"));

        assertThatThrownBy(() -> trainingService.addTraining(
                "John.Doe", "Nika.Doe", "", "Yoga", LocalDate.now(), 60))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Training name is required");
    }

    @Test
    void addTrainingThrowsWhenDurationIsZero() {
        when(traineeDao.findByUserName("John.Doe")).thenReturn(Optional.of(buildTrainee("John.Doe")));
        when(trainerDao.findByUserName("Nika.Doe")).thenReturn(Optional.of(buildTrainer("Nika.Doe")));
        when(trainingTypeDao.findByName("Yoga")).thenReturn(buildTrainingType("Yoga"));

        assertThatThrownBy(() -> trainingService.addTraining(
                "John.Doe", "Nika.Doe", "Morning Yoga", "Yoga", LocalDate.now(), 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duration must be positive");
    }

    @Test
    void getTraineeTrainingsReturnsResults() {
        List<Training> trainings = List.of(
                buildTraining("Morning Yoga", LocalDate.now()),
                buildTraining("Evening Yoga", LocalDate.now().minusDays(5))
        );
        when(trainingDao.findByTraineeUsername("John.Doe", null, null, null, null))
                .thenReturn(trainings);

        List<Training> result = trainingService.getTraineeTrainings("John.Doe", null, null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Training::getTrainingName)
                .containsExactlyInAnyOrder("Morning Yoga", "Evening Yoga");
    }

    @Test
    void getTraineeTrainingsPassesFiltersToDao() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        when(trainingDao.findByTraineeUsername("John.Doe", from, to, "Nika.Doe", "Yoga"))
                .thenReturn(List.of());

        trainingService.getTraineeTrainings("John.Doe", from, to, "Nika.Doe", "Yoga");

        verify(trainingDao).findByTraineeUsername("John.Doe", from, to, "Nika.Doe", "Yoga");
    }

    @Test
    void getTraineeTrainingsReturnsEmptyWhenNoMatch() {
        when(trainingDao.findByTraineeUsername("John.Doe", null, null, null, null))
                .thenReturn(List.of());

        List<Training> result = trainingService.getTraineeTrainings("John.Doe", null, null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void getTrainerTrainingsReturnsResults() {
        List<Training> trainings = List.of(
                buildTraining("Morning Yoga", LocalDate.now())
        );
        when(trainingDao.findByTrainerUsername("Nika.Doe", null, null, null))
                .thenReturn(trainings);

        List<Training> result = trainingService.getTrainerTrainings("Nika.Doe", null, null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getTrainerTrainingsPassesFiltersToDao() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        when(trainingDao.findByTrainerUsername("Nika.Doe", from, to, "John.Doe"))
                .thenReturn(List.of());

        trainingService.getTrainerTrainings("Nika.Doe", from, to, "John.Doe");

        verify(trainingDao).findByTrainerUsername("Nika.Doe", from, to, "John.Doe");
    }

    @Test
    void getTrainerTrainingsReturnsEmptyWhenNoMatch() {
        when(trainingDao.findByTrainerUsername("Nika.Doe", null, null, null))
                .thenReturn(List.of());

        List<Training> result = trainingService.getTrainerTrainings("Nika.Doe", null, null, null);

        assertThat(result).isEmpty();
    }

    private Trainee buildTrainee(String username) {
        return Trainee.builder()
                .user(User.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .username(username)
                        .password("pass123")
                        .isActive(true)
                        .build())
                .dateOfBirth(LocalDate.of(1999, 5, 6))
                .build();
    }

    private Trainer buildTrainer(String username) {
        return Trainer.builder()
                .user(User.builder()
                        .firstName("Nika")
                        .lastName("Doe")
                        .username(username)
                        .password("pass123")
                        .isActive(true)
                        .build())
                .specialization(buildTrainingType("Yoga"))
                .build();
    }

    private TrainingType buildTrainingType(String name) {
        return TrainingType.builder().trainingTypeName(name).build();
    }

    private Training buildTraining(String name, LocalDate date) {
        return Training.builder()
                .trainingName(name)
                .trainingDate(date)
                .trainingDuration(60)
                .trainingType(buildTrainingType("Yoga"))
                .build();
    }
}