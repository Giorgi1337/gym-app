package com.gym.controller;

import com.gym.dto.*;
import com.gym.exception.ResourceNotFoundException;
import com.gym.service.TraineeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(TraineeController.class)
public class TraineeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TraineeService traineeService;

    private TraineeRegistrationRequest validRegistrationRequest;
    private TraineeUpdateRequest validUpdateRequest;
    private UpdateTraineeTrainersRequest validUpdateTrainersRequest;
    private ActivationRequest validActivationRequest;

    @BeforeEach
    void setUp() {
        validRegistrationRequest = new TraineeRegistrationRequest();
        validRegistrationRequest.setFirstName("John");
        validRegistrationRequest.setLastName("Smith");

        validUpdateRequest = new TraineeUpdateRequest();
        validUpdateRequest.setFirstName("Johnny");
        validUpdateRequest.setLastName("Smithers");
        validUpdateRequest.setIsActive(true);

        TrainerRef trainerRef = new TrainerRef();
        trainerRef.setTrainerUsername("Levan.Tsereteli");
        validUpdateTrainersRequest = new UpdateTraineeTrainersRequest();
        validUpdateTrainersRequest.setTrainers(List.of(trainerRef));

        validActivationRequest = new ActivationRequest();
        validActivationRequest.setIsActive(false);
    }

    @Test
    void registerTraineeWithValidDataReturnsCreated() throws Exception {
        RegistrationResponse response = new RegistrationResponse("John.Smith", "pass123ABC");

        when(traineeService.save(any(TraineeRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/trainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("John.Smith"))
                .andExpect(jsonPath("$.password").value("pass123ABC"));
    }

    @Test
    void registerTraineeWithMissingFirstNameReturnsBadRequest() throws Exception {
        validRegistrationRequest.setFirstName(null);

        mockMvc.perform(post("/api/trainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }

    @Test
    void getProfileWithExistingUsernameReturnsOk() throws Exception {
        TraineeProfileResponse response = new TraineeProfileResponse()
                .firstName("John")
                .lastName("Smith")
                .isActive(true);

        when(traineeService.findByUsername("John.Smith")).thenReturn(response);

        mockMvc.perform(get("/api/trainee/{username}", "John.Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void getProfileWithUnknownUsernameReturnsNotFound() throws Exception {
        when(traineeService.findByUsername("unknown"))
                .thenThrow(new ResourceNotFoundException("Trainee not found: unknown"));

        mockMvc.perform(get("/api/trainee/{username}", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTraineeProfileWithValidDataReturnsOk() throws Exception {
        TraineeUpdateResponse response = new TraineeUpdateResponse()
                .username("John.Smith")
                .firstName("Johnny")
                .lastName("Smithers")
                .isActive(true);

        when(traineeService.update(eq("John.Smith"), any(TraineeUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/trainee/{username}", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.lastName").value("Smithers"));
    }

    @Test
    void updateTraineeProfileWithMissingIsActiveReturnsBadRequest() throws Exception {
        validUpdateRequest.setIsActive(null);

        mockMvc.perform(put("/api/trainee/{username}", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }

    @Test
    void updateTraineeProfileWithUnknownUsernameReturnsNotFound() throws Exception {
        when(traineeService.update(eq("unknown"), any(TraineeUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Trainee not found: unknown"));

        mockMvc.perform(put("/api/trainee/{username}", "unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTraineeProfileWithExistingUsernameReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/trainee/{username}", "John.Smith"))
                .andExpect(status().isOk());

        verify(traineeService).delete("John.Smith");
    }

    @Test
    void deleteTraineeProfileWithUnknownUsernameReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Trainee not found: unknown"))
                .when(traineeService).delete("unknown");

        mockMvc.perform(delete("/api/trainee/{username}", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUnassignedTrainersWithExistingUsernameReturnsOk() throws Exception {
        TrainerSummary summary = new TrainerSummary()
                .username("Levan.Tsereteli")
                .firstName("Levan")
                .lastName("Tsereteli")
                .specialization("Yoga");

        when(traineeService.getUnassignedTrainers("John.Smith")).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/trainee/{username}/unassigned-trainers", "John.Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Levan.Tsereteli"))
                .andExpect(jsonPath("$[0].specialization").value("Yoga"));
    }

    @Test
    void getUnassignedTrainersWithUnknownUsernameReturnsNotFound() throws Exception {
        when(traineeService.getUnassignedTrainers("unknown"))
                .thenThrow(new ResourceNotFoundException("Trainee not found: unknown"));

        mockMvc.perform(get("/api/trainee/{username}/unassigned-trainers", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTrainersWithValidDataReturnsOk() throws Exception {
        TrainerSummary summary = new TrainerSummary()
                .username("Levan.Tsereteli")
                .firstName("Levan")
                .lastName("Tsereteli")
                .specialization("Yoga");

        when(traineeService.updateTrainers(eq("John.Smith"), any(UpdateTraineeTrainersRequest.class)))
                .thenReturn(List.of(summary));

        mockMvc.perform(put("/api/trainee/{username}/trainers", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateTrainersRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Levan.Tsereteli"));
    }

    @Test
    void updateTrainersWithEmptyTrainerListReturnsBadRequest() throws Exception {
        validUpdateTrainersRequest.setTrainers(Collections.emptyList());

        mockMvc.perform(put("/api/trainee/{username}/trainers", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateTrainersRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }

    @Test
    void updateTrainersWithUnknownTrainerReturnsNotFound() throws Exception {
        when(traineeService.updateTrainers(eq("John.Smith"), any(UpdateTraineeTrainersRequest.class)))
                .thenThrow(new ResourceNotFoundException("Trainer(s) not found: [Unknown.Trainer]"));

        mockMvc.perform(put("/api/trainee/{username}/trainers", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateTrainersRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void setActiveWithValidDataReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/trainee/{username}/status", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validActivationRequest)))
                .andExpect(status().isOk());

        verify(traineeService).setActive("John.Smith", false);
    }

    @Test
    void setActiveWithMissingIsActiveReturnsBadRequest() throws Exception {
        validActivationRequest.setIsActive(null);

        mockMvc.perform(patch("/api/trainee/{username}/status", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validActivationRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }

    @Test
    void setActiveWithUnknownUsernameReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Trainee not found: unknown"))
                .when(traineeService).setActive("unknown", false);

        mockMvc.perform(patch("/api/trainee/{username}/status", "unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validActivationRequest)))
                .andExpect(status().isNotFound());
    }
}
