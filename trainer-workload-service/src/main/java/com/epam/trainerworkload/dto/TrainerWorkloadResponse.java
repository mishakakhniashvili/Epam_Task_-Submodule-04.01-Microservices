package com.epam.trainerworkload.dto;

import java.util.List;

public record TrainerWorkloadResponse(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        boolean active,
        List<YearWorkloadResponse> years
) {
}
