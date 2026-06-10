package com.gym.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public final class Trainer extends User {

    @Setter(AccessLevel.NONE)
    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("specialization")
    private String specialization;

    @Override
    public String toString() {
        return "Trainer [userId=%d | %s | specialization=%s]"
                .formatted(userId, super.toString(), specialization);
    }
}