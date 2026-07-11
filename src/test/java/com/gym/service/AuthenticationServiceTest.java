package com.gym.service;

import com.gym.repository.UserRepository;
import com.gym.exception.AuthenticationFailedException;
import com.gym.metrics.GymMetrics;
import com.gym.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class AuthenticationServiceTest {

    private UserRepository userRepository;
    private GymMetrics gymMetrics;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        gymMetrics = mock(GymMetrics.class);

        authenticationService = new AuthenticationService(userRepository, gymMetrics);
    }

    @Test
    void loginWithValidCredentialsSucceedsAndIncrementsSuccessMetric() {
        User user = buildUser("John.Smith", "correctPassword");

        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        authenticationService.login("John.Smith", "correctPassword");

        verify(gymMetrics).incrementLoginSuccess();
        verify(gymMetrics, never()).incrementLoginFailure();
    }

    @Test
    void loginWithUnknownUsernameThrowsAndIncrementsFailureMetric() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login("unknown", "anyPassword"))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Invalid username or password");

        verify(gymMetrics).incrementLoginFailure();
        verify(gymMetrics, never()).incrementLoginSuccess();
    }

    @Test
    void loginWithWrongPasswordThrowsAndIncrementsFailureMetric() {
        User user = buildUser("John.Smith", "correctPassword");

        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authenticationService.login("John.Smith", "wrongPassword"))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Invalid username or password");

        verify(gymMetrics).incrementLoginFailure();
        verify(gymMetrics, never()).incrementLoginSuccess();
    }

    @Test
    void changePasswordWithValidCredentialsUpdatesPassword() {
        User user = buildUser("John.Smith", "oldPassword");

        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        authenticationService.changePassword("John.Smith", "oldPassword", "newPassword");

        assertThat(user.getPassword()).isEqualTo("newPassword");
    }

    @Test
    void changePasswordWithUnknownUsernameThrows() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.changePassword("unknown", "oldPassword", "newPassword"))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void changePasswordWithWrongOldPasswordThrowsAndDoesNotChangePassword() {
        User user = buildUser("John.Smith", "correctOldPassword");

        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authenticationService.changePassword("John.Smith", "wrongOldPassword", "newPassword"))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Invalid username or password");

        assertThat(user.getPassword()).isEqualTo("correctOldPassword");
    }

    private User buildUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }
}
