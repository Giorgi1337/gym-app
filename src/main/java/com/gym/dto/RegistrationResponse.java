package com.gym.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credentials generated for a newly registered user")
public record RegistrationResponse(

        @Schema(description = "Generated unique username", example = "Nika.Beridze")
        String username,

        @Schema(description = "Generated password", example = "qW3rT8yUiO")
        String password
) {

}