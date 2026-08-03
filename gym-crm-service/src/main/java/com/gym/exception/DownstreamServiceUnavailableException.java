package com.gym.exception;

public class DownstreamServiceUnavailableException extends GymAppException {
    public DownstreamServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
