package com.epam.gymcrm.dto.trainee;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TraineeTrainingResponse {
    private String trainingName;
    private LocalDate trainingDate;
    private String trainingType;
    private int trainingDuration;
    private String trainerName;
}
