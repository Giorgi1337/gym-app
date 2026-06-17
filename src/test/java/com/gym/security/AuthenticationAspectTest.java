package com.gym.security;

import com.gym.exception.AuthenticationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AuthenticationAspectTest.TestConfig.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class AuthenticationAspectTest {

    private final DummyService dummyService;

    public AuthenticationAspectTest(DummyService dummyService) {
        this.dummyService = dummyService;
    }

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
    }

    @Test
    void throwsWhenNotAuthenticated() {
        assertThatThrownBy(dummyService::protectedMethod)
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("No authenticated user in context");
    }

    @Test
    void succeedsWhenAuthenticated() {
        SecurityContext.setCurrentUsername("John.Doe");

        assertThatNoException().isThrownBy(dummyService::protectedMethod);
    }

    @Test
    void unprotectedMethodPassesWithoutAuth() {
        assertThatNoException().isThrownBy(dummyService::unprotectedMethod);
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        public AuthenticationAspect authenticationAspect() {
            return new AuthenticationAspect();
        }

        @Bean
        public DummyService dummyService() {
            return new DummyService();
        }
    }

    static class DummyService {

        @RequiresAuthentication
        public void protectedMethod() {}

        public void unprotectedMethod() {}
    }
}