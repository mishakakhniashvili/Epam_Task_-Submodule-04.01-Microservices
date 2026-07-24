package com.epam.gymcrm.dto.trainer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrainerShortResponse {
    String trainerUsername;

    String trainerFirstName;

    String trainerLastName;

    String trainerSpecialization;
}
