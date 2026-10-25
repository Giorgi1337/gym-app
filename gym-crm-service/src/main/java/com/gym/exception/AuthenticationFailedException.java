package com.gym.exception;

public class AuthenticationFailedException extends GymAppException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}