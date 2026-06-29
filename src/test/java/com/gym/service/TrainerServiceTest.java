package com.gym.service;

import com.gym.dao.TrainerDao;
import com.gym.dao.TrainingTypeDao;
import com.gym.dto.RegistrationResponse;
import com.gym.dto.trainer.TrainerProfileResponse;
import com.gym.dto.trainer.TrainerRegistrationRequest;
import com.gym.dto.trainer.TrainerUpdateRequest;
import com.gym.dto.trainer.TrainerUpdateResponse;
import com.gym.exception.ResourceNotFoundException;
import com.gym.mapper.TrainerMapper;
import com.gym.model.Trainer;
import com.gym.model.TrainingType;
import com.gym.model.User;
import com.gym.utils.PasswordGenerator;
import com.gym.utils.UsernameGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class TrainerServiceTest {

    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;
    private UsernameGenerator usernameGenerator;
    private TrainerService trainerService;

    private MockedStatic<PasswordGenerator> passwordGeneratorMock;
    private MockedStatic<TrainerMapper> trainerMapperMock;

    @BeforeEach
    void setUp() {
        trainerDao = mock(TrainerDao.class);
        trainingTypeDao = mock(TrainingTypeDao.class);
        usernameGenerator = mock(UsernameGenerator.class);

        trainerService = new TrainerService(trainerDao, trainingTypeDao, usernameGenerator);

        passwordGeneratorMock = mockStatic(PasswordGenerator.class);
        trainerMapperMock = mockStatic(TrainerMapper.class);
    }

    @AfterEach
    void tearDown() {
        passwordGeneratorMock.close();
        trainerMapperMock.close();
    }

    @Test
    void registerShouldNormalizeNamesLookupSpecializationAndSaveTrainer() {
        TrainerRegistrationRequest request = mock(TrainerRegistrationRequest.class);
        RegistrationResponse expectedResponse = mock(RegistrationResponse.class);

        User user = new User();
        user.setFirstName("  john  ");
        user.setLastName("smith");

        TrainingType requestSpecialization = new TrainingType();
        requestSpecialization.setTrainingTypeName("  Boxing  ");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(requestSpecialization);

        TrainingType dbSpecialization = new TrainingType();
        dbSpecialization.setTrainingTypeName("Boxing");

        trainerMapperMock.when(() -> TrainerMapper.toEntity(request)).thenReturn(trainer);
        passwordGeneratorMock.when(PasswordGenerator::generate).thenReturn("pass123ABC");
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(trainingTypeDao.findByName("Boxing")).thenReturn(dbSpecialization);
        trainerMapperMock.when(() -> TrainerMapper.toRegistrationResponse(trainer)).thenReturn(expectedResponse);

        RegistrationResponse actualResponse = trainerService.register(request);

        assertThat(trainer.getUser().getFirstName()).isEqualTo("John");
        assertThat(trainer.getUser().getLastName()).isEqualTo("Smith");
        assertThat(trainer.getUser().getUsername()).isEqualTo("John.Smith");
        assertThat(trainer.getUser().getPassword()).isEqualTo("pass123ABC");
        assertThat(trainer.getSpecialization()).isEqualTo(dbSpecialization);

        verify(trainerDao).save(trainer);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void registerWhenSpecializationNotFoundThrowsResourceNotFoundException() {
        TrainerRegistrationRequest request = mock(TrainerRegistrationRequest.class);

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Smith");

        TrainingType requestSpecialization = new TrainingType();
        requestSpecialization.setTrainingTypeName("UnknownType");

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(requestSpecialization);

        trainerMapperMock.when(() -> TrainerMapper.toEntity(request)).thenReturn(trainer);
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(trainingTypeDao.findByName("UnknownType")).thenReturn(null);

        assertThatThrownBy(() -> trainerService.register(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Specialization not found: UnknownType");

        verify(trainerDao, never()).save(any());
    }

    @Test
    void getProfileWithExistingUsernameReturnsProfileResponse() {
        Trainer trainer = new Trainer();
        TrainerProfileResponse expectedResponse = mock(TrainerProfileResponse.class);

        when(trainerDao.findByUserName("John.Smith")).thenReturn(Optional.of(trainer));
        trainerMapperMock.when(() -> TrainerMapper.toProfileResponse(trainer)).thenReturn(expectedResponse);

        TrainerProfileResponse actualResponse = trainerService.getProfile("John.Smith");

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void getProfileWithUnknownUsernameThrowsResourceNotFoundException() {
        when(trainerDao.findByUserName("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getProfile("unknown"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainer not found: unknown");
    }

    @Test
    void updateWithValidDataUpdatesAndReturnsResponse() {
        TrainerUpdateRequest request = mock(TrainerUpdateRequest.class);
        TrainerUpdateResponse expectedResponse = mock(TrainerUpdateResponse.class);

        User user = new User();
        user.setUsername("John.Smith");
        Trainer trainer = new Trainer();
        trainer.setUser(user);

        when(trainerDao.findByUserName("John.Smith")).thenReturn(Optional.of(trainer));
        trainerMapperMock.when(() -> TrainerMapper.toUpdateResponse(trainer)).thenReturn(expectedResponse);

        TrainerUpdateResponse actualResponse = trainerService.update("John.Smith", request);

        trainerMapperMock.verify(() -> TrainerMapper.applyUpdate(trainer, request));
        verify(trainerDao).update(trainer);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void updateWithUnknownUsernameThrowsResourceNotFoundException() {
        TrainerUpdateRequest request = mock(TrainerUpdateRequest.class);
        when(trainerDao.findByUserName("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.update("unknown", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainer not found: unknown");

        verify(trainerDao, never()).update(any());
    }

    @Test
    void setActiveWithTrueValueUpdatesStatusToActive() {
        TrainerUpdateResponse expectedResponse = mock(TrainerUpdateResponse.class);

        User user = new User();
        user.setIsActive(false);
        Trainer trainer = new Trainer();
        trainer.setUser(user);

        when(trainerDao.findByUserName("John.Smith")).thenReturn(Optional.of(trainer));
        trainerMapperMock.when(() -> TrainerMapper.toUpdateResponse(trainer)).thenReturn(expectedResponse);

        TrainerUpdateResponse actualResponse = trainerService.setActive("John.Smith", true);

        assertThat(trainer.getUser().getIsActive()).isTrue();
        verify(trainerDao).update(trainer);
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void setActiveWithUnknownUsernameThrowsResourceNotFoundException() {
        when(trainerDao.findByUserName("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.setActive("unknown", true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Trainer not found: unknown");

        verify(trainerDao, never()).update(any());
    }
}