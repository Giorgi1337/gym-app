package com.gym.service;

import com.gym.dao.UserDao;
import com.gym.exception.AuthenticationException;
import com.gym.model.User;
import com.gym.security.SecurityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthenticationServiceTest {

    private AuthenticationService authenticationService;
    private UserDao userDao;

    @BeforeEach
    void setup() {
        userDao = mock(UserDao.class);
        authenticationService = new AuthenticationService(userDao);
    }

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
    }

    @Test
    void loginSetsCurrentUsername() {
        when(userDao.findByUserName("John.Doe"))
                .thenReturn(
                        Optional.of(buildUser("John.Doe", "pass123"))
                );

        authenticationService.login("John.Doe", "pass123");

        assertThat(SecurityContext.getCurrentUsername()).isEqualTo("John.Doe");
    }

    @Test
    void loginThrowsWhenUsernameNotFound() {
        when(userDao.findByUserName("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login("unknown", "pass123"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void loginThrowsWhenPasswordIsWrong() {
        when(userDao.findByUserName("John.Doe")).thenReturn(Optional.of(buildUser("John.Doe", "pass123")));

        assertThatThrownBy(() -> authenticationService.login("John.Doe", "wrongpassword"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid username or password");
    }

    @Test
    void logoutClearsCurrentUsername() {
        when(userDao.findByUserName("John.Doe")).thenReturn(Optional.of(buildUser("John.Doe", "pass123")));

        authenticationService.login("John.Doe", "pass123");
        authenticationService.logout();

        assertThatThrownBy(SecurityContext::getCurrentUsername)
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("No authenticated user in context");
    }

    @Test
    void loginDoesNotRevealWhetherUsernameExists() {
        // wrong username and wrong password must produce identical message
        // to prevent user enumeration attacks
        when(userDao.findByUserName("unknown")).thenReturn(Optional.empty());
        when(userDao.findByUserName("John.Doe")).thenReturn(Optional.of(buildUser("John.Doe", "pass123")));

        Throwable wrongUsername = catchThrowable(() ->
                authenticationService.login("unknown", "pass123"));
        Throwable wrongPassword = catchThrowable(() ->
                authenticationService.login("John.Doe", "wrongpassword"));

        assertThat(wrongUsername).hasMessage(wrongPassword.getMessage());
    }

    private User buildUser(String username, String password) {
        return User.builder()
                .username(username)
                .password(password)
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();
    }

}
