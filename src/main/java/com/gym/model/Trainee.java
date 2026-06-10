package com.gym.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public final class Trainee extends User {

    @Setter(AccessLevel.NONE)
    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("dateOfBirth")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @JsonProperty("address")
    private String address;

    @Override
    public String toString() {
        return "Trainee [userId=%d | %s | dob=%s | address=%s]"
                .formatted(userId, super.toString(), dateOfBirth, address);
    }
}