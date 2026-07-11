package com.gym.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class BusinessValidationException extends GymAppException {

    private final List<ErrorResponse.FieldError> errors;

    public BusinessValidationException(List<ErrorResponse.FieldError> errors) {
        super("Validation failed");
        this.errors = errors;
    }

}