package com.gym.model;

import com.gym.validation.OnCreate;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Entity
@Table(name = "training_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Training type name is required", groups = OnCreate.class)
    @Column(name = "training_type_name", nullable = false, unique = true)
    private String trainingTypeName;
}