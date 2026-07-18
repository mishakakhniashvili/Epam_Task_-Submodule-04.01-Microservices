package com.epam.gymcrm.dto.trainee;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TraineeTrainingResponse {
    private String trainingName;
    private String trainingDate;
    private String trainingType;
    private int trainingDuration;
    private String trainerName;
}
