package com.gym.dto.trainee;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

@Schema(description = "Payload for registering a new trainee")
public record TraineeRegistrationRequest(

        @Schema(description = "Trainee's first name", example = "Nika")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(description = "Trainee's last name", example = "Beridze")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(description = "Date of birth", example = "1998-05-12")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Schema(description = "Home address", example = "12 Rustaveli Ave, Tbilisi")
        String address
) {}