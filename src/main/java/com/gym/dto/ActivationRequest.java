package com.gym.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload for activating or deactivating a user profile")
public record ActivationRequest(

        @Schema(description = "Desired active state", example = "false")
        @NotNull(message = "isActive is required")
        Boolean isActive

) {
}