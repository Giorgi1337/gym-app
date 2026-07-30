package com.gym.utils;

import com.gym.exception.BusinessValidationException;
import com.gym.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsernameGeneratorTest {

    private UserRepository userRepository;
    private UsernameGenerator usernameGenerator;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        usernameGenerator = new UsernameGenerator(userRepository);
    }

    @Test
    void shouldReturnBaseUsernameWhenNotExists() {
        when(userRepository.findUsernamesStartingWith("john.doe")).
                thenReturn(List.of());

        String result = usernameGenerator.generate("john", "doe");

        assertThat(result).isEqualTo("john.doe");
    }

    @Test
    void shouldReturnNextAvailableSuffixWhenBaseExists() {
        when(userRepository.findUsernamesStartingWith("john.doe"))
                .thenReturn(List.of(
                        "john.doe",
                        "john.doe1",
                        "john.doe2"
                ));

        String result = usernameGenerator.generate("john", "doe");

        assertThat(result).isEqualTo("john.doe3");
    }

    @Test
    void shouldSkipGapsAndFindFirstAvailableSuffix() {
        when(userRepository.findUsernamesStartingWith("john.doe"))
                .thenReturn(List.of(
                        "john.doe",
                        "john.doe1",
                        "john.doe3"
                ));

        String result = usernameGenerator.generate("john", "doe");

        assertThat(result).isEqualTo("john.doe2");
    }

    @Test
    void shouldThrowExceptionWhenFirstNameIsBlank() {
        assertThatThrownBy(() -> usernameGenerator.generate("", "doe"))
                .isInstanceOf(BusinessValidationException.class)
                .satisfies(ex -> {
                    BusinessValidationException exception = (BusinessValidationException) ex;
                    assertThat(exception.getErrors())
                            .hasSize(1)
                            .extracting("field")
                            .containsExactly("firstName");
                });
    }

    @Test
    void shouldThrowExceptionWhenLastNameIsBlank() {
        assertThatThrownBy(() -> usernameGenerator.generate(" ", " "))
                .isInstanceOf(BusinessValidationException.class)
                .satisfies(ex -> {
                    BusinessValidationException exception = (BusinessValidationException) ex;
                    assertThat(exception.getErrors())
                            .hasSize(2)
                            .extracting("field")
                            .containsExactlyInAnyOrder("firstName", "lastName");
                });
    }
}