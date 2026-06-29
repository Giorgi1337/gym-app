package com.gym.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credentials used to authenticate a user")
public record AuthRequest(

        @Schema(description = "Username", example = "Levan.Tsereteli")
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "Password", example = "aZ7kP2qLmX")
        @NotBlank(message = "Password is required")
        String password

) {}