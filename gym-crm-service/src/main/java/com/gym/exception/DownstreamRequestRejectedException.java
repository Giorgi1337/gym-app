package com.gym.exception;

public class DownstreamRequestRejectedException extends GymAppException {
    public DownstreamRequestRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
