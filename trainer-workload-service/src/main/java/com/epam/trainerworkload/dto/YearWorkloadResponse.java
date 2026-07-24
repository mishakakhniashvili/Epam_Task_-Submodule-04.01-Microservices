package com.epam.trainerworkload.dto;

import java.util.List;

public record YearWorkloadResponse(
        int year,
        List<MonthWorkloadResponse> months
) {
}
