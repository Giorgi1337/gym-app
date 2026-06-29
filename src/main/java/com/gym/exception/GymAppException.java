package com.gym.exception;

public abstract class GymAppException extends RuntimeException {
    public GymAppException(String message) {
        super(message);
    }
}