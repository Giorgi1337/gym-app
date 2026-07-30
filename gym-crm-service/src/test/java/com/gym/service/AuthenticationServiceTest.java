package com.gym.service;

import com.gym.repository.UserRepository;
import com.gym.exception.AuthenticationFailedException;
import com.gym.metrics.GymMetrics;
import com.gym.model.User;
import com.gym.security.BruteForceProtectionService;
import com.gym.security.JwtService;
import com.gym.security.TokenBlacklistService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class AuthenticationServiceTest {

    private UserRepository userRepository;
    private GymMetrics gymMetrics;
    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private BruteForceProtectionService bruteForceProtectionService;
    private TokenBlacklistService tokenBlacklistService;
    private PasswordEncoder passwordEncoder;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        gymMetrics = mock(GymMetrics.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        bruteForceProtectionService = mock(BruteForceProtectionService.class);
        tokenBlacklistService = mock(TokenBlacklistService.class);
        passwordEncoder = mock(PasswordEncoder.class);

        authenticationService = new AuthenticationService(
                userRepository,
                gymMetrics,
                authenticationManager,
                jwtService,
                bruteForceProtectionService,
                tokenBlacklistService,
                passwordEncoder);
    }

    @Test
    void loginWithValidCredentialsSucceedsAndIncrementsSuccessMetric() {
        when(jwtService.generateToken("John.Smith")).thenReturn("jwt-token");

        var response = authenticationService.login("John.Smith", "correctPassword");

        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(bruteForceProtectionService).checkNotBlocked("John.Smith");
        verify(bruteForceProtectionService).registerSuccess("John.Smith");
        verify(gymMetrics).incrementLoginSuccess();
        verify(gymMetrics, never()).incrementLoginFailure();
    }

    @Test
    void loginWithUnknownUsernameThrowsAndIncrementsFailureMetric() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.login("unknown", "anyPassword"))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Invalid username or password");

        verify(gymMetrics).incrementLoginFailure();
        verify(bruteForceProtectionService).registerFailure("unknown");
        verify(gymMetrics, never()).incrementLoginSuccess();
    }

    @Test
    void loginWithWrongPasswordThrowsAndIncrementsFailureMetric() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authenticationService.login("John.Smith", "wrongPassword"))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Invalid username or password");

        verify(gymMetrics).incrementLoginFailure();
        verify(bruteForceProtectionService).registerFailure("John.Smith");
        verify(gymMetrics, never()).incrementLoginSuccess();
    }

    @Test
    void changePasswordWithValidCredentialsUpdatesPassword() {
        User user = buildUser("John.Smith", "oldPassword");

        when(userRepository.findByUsername("John.Smith")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "oldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        authenticationService.changePassword("John.Smith", "oldPassword", "newPassword");

        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
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
        when(passwordEncoder.matches("wrongOldPassword", "correctOldPassword")).thenReturn(false);

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
