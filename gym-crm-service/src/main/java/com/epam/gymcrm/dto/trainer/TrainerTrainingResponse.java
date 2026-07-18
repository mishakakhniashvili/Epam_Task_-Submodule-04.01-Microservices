package com.epam.gymcrm.dto.trainer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrainerTrainingResponse {
    private String trainingName;
    private String trainingDate;
    private String trainingType;
    private int trainingDuration;
    private String traineeName;
}
