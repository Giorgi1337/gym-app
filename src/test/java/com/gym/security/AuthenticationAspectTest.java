package com.gym.security;

import com.gym.exception.AuthenticationFailedException;
import com.gym.service.AuthenticationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationAspectTest {

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationAspect authenticationAspect;

    @BeforeEach
    void setupRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Username", "John.Doe");
        request.addHeader("X-Password", "pass123");
        request.setMethod("POST");
        request.setRequestURI("/api/gym/exercise");

        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void AuthWithValidCredentialsSuccessfully() {
        assertThatNoException()
                .isThrownBy(() -> authenticationAspect.checkAuth());

        verify(authenticationService).login("John.Doe", "pass123");
    }

    @Test
    void AuthWithInvalidCredentialsPropagatesAuthenticationException() {
        doThrow(new AuthenticationFailedException("Invalid username or password"))
                .when(authenticationService)
                .login("John.Doe", "pass123");


        assertThatThrownBy(() -> authenticationAspect.checkAuth())
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid username or password");
    }
}