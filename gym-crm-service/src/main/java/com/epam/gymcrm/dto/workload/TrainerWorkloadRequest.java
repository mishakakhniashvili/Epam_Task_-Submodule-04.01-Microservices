package com.epam.gymcrm.dto.workload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
@Builder
@Getter
@AllArgsConstructor
public class TrainerWorkloadRequest {
    private String trainerUsername;

    private String trainerFirstName;

    private String trainerLastName;

    private Boolean active ;

    private LocalDate trainingDate ;

    private Integer trainingDuration;

    private ActionType actionType ;

    private String eventId;
}
