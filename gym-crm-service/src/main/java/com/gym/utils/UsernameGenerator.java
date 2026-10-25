package com.gym.utils;

import com.gym.exception.BusinessValidationException;
import com.gym.exception.ErrorResponse;
import com.gym.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UsernameGenerator {

    private final UserRepository userRepository;

    public UsernameGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generate(String firstName, String lastName) {
        String base = buildBase(firstName, lastName);

        List<String> existing = userRepository.findUsernamesStartingWith(base);

        if (!existing.contains(base)) {
            return base;
        }

        int suffix = 1;

        while (existing.contains(base + suffix)) {
            suffix++;
        }

        return base + suffix;
    }

    private String buildBase(String firstName, String lastName) {
        List<ErrorResponse.FieldError> errors = new ArrayList<>();

        if (firstName == null || firstName.isBlank()) {
            errors.add(new ErrorResponse.FieldError("firstName", "must not be blank"));
        }

        if (lastName == null || lastName.isBlank()) {
            errors.add(new ErrorResponse.FieldError("lastName", "must not be blank"));
        }

        if (!errors.isEmpty()) {
            throw new BusinessValidationException(errors);
        }

        return firstName.trim() + "." + lastName.trim();
    }
}

