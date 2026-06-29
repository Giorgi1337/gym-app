package com.gym.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload for changing a user's password")
public record ChangePasswordRequest(

        @Schema(description = "Current password", example = "aZ7kP2qLmX9w")
        @NotBlank(message = "Old password is required")
        String oldPassword,

        @Schema(description = "New password", example = "qW3rT8yUiO1p")
        @NotBlank(message = "New password is required")
        String newPassword

) {
}