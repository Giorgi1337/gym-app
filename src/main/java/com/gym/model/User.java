package com.gym.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode
public abstract sealed class User permits Trainer, Trainee {

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("username")
    private String username;

    @Getter(AccessLevel.NONE)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonProperty("isActive")
    private boolean isActive;

    @Override
    public String toString() {
        return "firstName=%s | lastName=%s | username=%s | isActive=%b"
                .formatted(firstName, lastName, username, isActive);
    }
}
