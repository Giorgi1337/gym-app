package com.gym.controller;

import com.gym.dto.AuthRequest;
import com.gym.dto.ChangePasswordRequest;
import com.gym.exception.AuthenticationFailedException;
import com.gym.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    private AuthRequest validAuthRequest;
    private ChangePasswordRequest validChangePasswordRequest;

    @BeforeEach
    void setUp() {
        validAuthRequest = new AuthRequest();
        validAuthRequest.setUsername("John.Smith");
        validAuthRequest.setPassword("correctPassword");

        validChangePasswordRequest = new ChangePasswordRequest();
        validChangePasswordRequest.setOldPassword("oldPassword");
        validChangePasswordRequest.setNewPassword("newPassword");
    }

    @Test
    void loginWithValidCredentialsReturnsOk() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isOk());

        verify(authenticationService).login("John.Smith", "correctPassword");
    }

    @Test
    void loginWithInvalidCredentialsReturnsUnauthorized() throws Exception {
        validAuthRequest.setPassword("wrongPassword");

        doThrow(new AuthenticationFailedException("Invalid username or password"))
                .when(authenticationService).login("John.Smith", "wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithMissingUsernameReturnsBadRequest() throws Exception {
        validAuthRequest.setUsername(null);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuthRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService);
    }

    @Test
    void changePasswordWithValidDataReturnsOk() throws Exception {
        mockMvc.perform(put("/api/auth/{username}/password", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validChangePasswordRequest)))
                .andExpect(status().isOk());

        verify(authenticationService).changePassword("John.Smith", "oldPassword", "newPassword");
    }

    @Test
    void changePasswordWithWrongOldPasswordReturnsUnauthorized() throws Exception {
        validChangePasswordRequest.setOldPassword("wrongOldPassword");

        doThrow(new AuthenticationFailedException("Invalid username or password"))
                .when(authenticationService).changePassword("John.Smith", "wrongOldPassword", "newPassword");

        mockMvc.perform(put("/api/auth/{username}/password", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validChangePasswordRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePasswordWithMissingNewPasswordReturnsBadRequest() throws Exception {
        validChangePasswordRequest.setNewPassword(null);

        mockMvc.perform(put("/api/auth/{username}/password", "John.Smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validChangePasswordRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService);
    }
}
