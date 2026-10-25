package com.gym.controller;

import com.gym.dto.*;
import com.gym.exception.ResourceNotFoundException;
import com.gym.security.JwtAuthenticationFilter;
import com.gym.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainerController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainerService trainerService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private TrainerRegistrationRequest validRegistrationRequest;
    private TrainerUpdateRequest validUpdateRequest;
    private ActivationRequest validActivationRequest;

    @BeforeEach
    void setUp() {
        validRegistrationRequest = new TrainerRegistrationRequest();
        validRegistrationRequest.setFirstName("John");
        validRegistrationRequest.setLastName("Smith");
        validRegistrationRequest.setSpecialization("Yoga");

        validUpdateRequest = new TrainerUpdateRequest();
        validUpdateRequest.setFirstName("Johnny");
        validUpdateRequest.setLastName("Smithers");
        validUpdateRequest.setIsActive(true);

        validActivationRequest = new ActivationRequest();
        validActivationRequest.setIsActive(false);
    }

    @Test
    void registerTrainerWithValidDataReturnsCreated() throws Exception {
        RegistrationResponse response = new RegistrationResponse("John.Smith", "pass123ABC");

        when(trainerService.save(any(TrainerRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("John.Smith"))
                .andExpect(jsonPath("$.password").value("pass123ABC"));

        verify(trainerService).save(any(TrainerRegistrationRequest.class));
    }

    @Test
    void registerTrainerWithMissingFirstNameReturnsBadRequest() throws Exception {
        validRegistrationRequest.setFirstName(null);

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    @Test
    void getTrainerProfileWithExistingUsernameReturnsOk() throws Exception {
        TrainerProfileResponse response = new TrainerProfileResponse()
                .firstName("John")
                .lastName("Smith")
                .specialization("Yoga")
                .isActive(true);

        when(trainerService.findByUsername("John.Smith")).thenReturn(response);

        mockMvc.perform(get("/api/trainers/{username}", "John.Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.specialization").value("Yoga"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void getTrainerProfileWithUnknownUsernameReturnsNotFound() throws Exception {
        when(trainerService.findByUsername("unknown"))
                .thenThrow(new ResourceNotFoundException("Trainer not found: unknown"));

        mockMvc.perform(get("/api/trainers/{username}", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTrainerProfileWithValidDataReturnsOk() throws Exception {
        TrainerUpdateResponse response = new TrainerUpdateResponse()
                .username("John.Smith")
                .firstName("Johnny")
                .lastName("Smithers")
                .specialization("Yoga")
                .isActive(true);

        when(trainerService.update(eq("John.Smith"), any(TrainerUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/trainers/{username}", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.lastName").value("Smithers"));
    }

    @Test
    void updateTrainerProfileWithMissingIsActiveReturnsBadRequest() throws Exception {
        validUpdateRequest.setIsActive(null);

        mockMvc.perform(put("/api/trainers/{username}", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    @Test
    void updateTrainerProfileWithUnknownUsernameReturnsNotFound() throws Exception {
        when(trainerService.update(eq("unknown"), any(TrainerUpdateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Trainer not found: unknown"));

        mockMvc.perform(put("/api/trainers/{username}", "unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void setTrainerActiveWithValidDataReturnsOk() throws Exception {
        mockMvc.perform(patch("/api/trainers/{username}/status", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validActivationRequest)))
                .andExpect(status().isOk());

        verify(trainerService).setActive("John.Smith", false);
    }

    @Test
    void setTrainerActiveWithMissingIsActiveReturnsBadRequest() throws Exception {
        validActivationRequest.setIsActive(null);

        mockMvc.perform(patch("/api/trainers/{username}/status", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validActivationRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    @Test
    void setTrainerActiveWithUnknownUsernameReturnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Trainer not found: unknown"))
                .when(trainerService).setActive("unknown", false);

        mockMvc.perform(patch("/api/trainers/{username}/status", "unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validActivationRequest)))
                .andExpect(status().isNotFound());
    }
}
