package com.gym.controller;

import com.gym.dto.*;
import com.gym.exception.ResourceNotFoundException;
import com.gym.security.JwtAuthenticationFilter;
import com.gym.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private AddTrainingRequest validAddTrainingRequest;

    @BeforeEach
    void setUp() {
        validAddTrainingRequest = new AddTrainingRequest();
        validAddTrainingRequest.setTrainingName("Morning Yoga");
        validAddTrainingRequest.setTrainingDate(OffsetDateTime.parse("2026-06-25T09:00:00+04:00"));
        validAddTrainingRequest.setTrainingDuration(60);
    }

    @Test
    void getTraineeTrainingsWithNoQueryParamsUsesDefaultPaging() throws Exception {
        TraineeTrainingPageResponse response = new TraineeTrainingPageResponse()
                .content(List.of())
                .page(0)
                .size(20)
                .totalElements(0L)
                .totalPages(0);

        when(trainingService.getTraineeTrainings(
                eq("John.Smith"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/trainees/{username}/trainings", "John.Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(trainingService).getTraineeTrainings(
                eq("John.Smith"), isNull(), isNull(), isNull(), isNull(), pageableCaptor.capture());

        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void getTraineeTrainingsWithFiltersAndPagingBindsAllParams() throws Exception {
        TraineeTrainingResponse entry = new TraineeTrainingResponse()
                .trainingName("Morning Yoga")
                .trainingDate(OffsetDateTime.parse("2026-06-15T09:00:00+04:00"))
                .trainingType("Yoga")
                .trainingDuration(60)
                .trainerName("Levan Tsereteli");

        TraineeTrainingPageResponse response = new TraineeTrainingPageResponse()
                .content(List.of(entry))
                .page(1)
                .size(5)
                .totalElements(6L)
                .totalPages(2);

        when(trainingService.getTraineeTrainings(
                eq("John.Smith"), any(OffsetDateTime.class), any(OffsetDateTime.class),
                eq("Levan.Tsereteli"), eq("Yoga"), any(Pageable.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/trainees/{username}/trainings", "John.Smith")
                        .param("periodFrom", "2026-06-01T00:00:00+04:00")
                        .param("periodTo", "2026-06-30T23:59:59+04:00")
                        .param("trainerName", "Levan.Tsereteli")
                        .param("trainingType", "Yoga")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "trainingDate,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].trainingName").value("Morning Yoga"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(6));

        ArgumentCaptor<OffsetDateTime> fromCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> toCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(trainingService).getTraineeTrainings(
                eq("John.Smith"), fromCaptor.capture(), toCaptor.capture(),
                eq("Levan.Tsereteli"), eq("Yoga"), pageableCaptor.capture());

        assertThat(fromCaptor.getValue().toInstant())
                .isEqualTo(OffsetDateTime.parse("2026-05-31T20:00:00Z").toInstant());
        assertThat(toCaptor.getValue().toInstant())
                .isEqualTo(OffsetDateTime.parse("2026-06-30T19:59:59Z").toInstant());
        Pageable resolved = pageableCaptor.getValue();
        assertThat(resolved.getPageNumber()).isEqualTo(1);
        assertThat(resolved.getPageSize()).isEqualTo(5);
        assertThat(Objects.requireNonNull(resolved.getSort().getOrderFor("trainingDate")).getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getTraineeTrainingsWithUnknownUsernameReturnsNotFound() throws Exception {
        when(trainingService.getTraineeTrainings(
                eq("unknown"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("Trainee not found: unknown"));

        mockMvc.perform(get("/api/trainees/{username}/trainings", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTrainerTrainingsWithValidUsernameReturnsOk() throws Exception {
        TrainerTrainingResponse entry = new TrainerTrainingResponse()
                .trainingName("Morning Yoga")
                .trainingDate(OffsetDateTime.parse("2026-06-15T09:00:00+04:00"))
                .trainingType("Yoga")
                .trainingDuration(60)
                .traineeName("John Smith");

        TrainerTrainingPageResponse response = new TrainerTrainingPageResponse()
                .content(List.of(entry))
                .page(0)
                .size(20)
                .totalElements(1L)
                .totalPages(1);

        when(trainingService.getTrainerTrainings(
                eq("Levan.Tsereteli"), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/trainers/{username}/trainings", "Levan.Tsereteli"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].traineeName").value("John Smith"));
    }

    @Test
    void getTrainerTrainingsWithUnknownUsernameReturnsNotFound() throws Exception {
        when(trainingService.getTrainerTrainings(
                eq("unknown"), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("Trainer not found: unknown"));

        mockMvc.perform(get("/api/trainers/{username}/trainings", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTrainingWithValidDataReturnsOk() throws Exception {
        mockMvc.perform(post("/api/trainers/{trainerUsername}/trainees/{traineeUsername}/trainings",
                        "Levan.Tsereteli", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddTrainingRequest)))
                .andExpect(status().isOk());

        verify(trainingService).addTraining(
                eq("Levan.Tsereteli"), eq("John.Smith"), any(AddTrainingRequest.class));
    }

    @Test
    void addTrainingWithMissingTrainingNameReturnsBadRequest() throws Exception {
        validAddTrainingRequest.setTrainingName(null);

        mockMvc.perform(post("/api/trainers/{trainerUsername}/trainees/{traineeUsername}/trainings",
                        "Levan.Tsereteli", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddTrainingRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainingService);
    }

    @Test
    void addTrainingWithUnknownTrainerReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Trainer not found: unknown"))
                .when(trainingService).addTraining(eq("unknown"), eq("John.Smith"), any(AddTrainingRequest.class));

        mockMvc.perform(post("/api/trainers/{trainerUsername}/trainees/{traineeUsername}/trainings",
                        "unknown", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAddTrainingRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getTrainingTypesReturnsOkWithList() throws Exception {
        TrainingTypeResponse type = new TrainingTypeResponse()
                .id(1L)
                .trainingTypeName("Yoga");

        when(trainingService.getTrainingTypes()).thenReturn(List.of(type));

        mockMvc.perform(get("/api/trainings/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingTypeName").value("Yoga"));
    }
}
