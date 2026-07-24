package com.epam.trainerworkload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadRequest {
    @NotBlank
    private String trainerUsername;

    @NotBlank
    private String trainerFirstName;

    @NotBlank
    private String trainerLastName;

    @NotNull
    private Boolean active;

    @NotNull
    private LocalDate trainingDate;

    @NotNull
    @Positive
    private Integer trainingDuration;

    @NotNull
    private ActionType actionType;

    @Size(max = 100)
    private String eventId;
}
