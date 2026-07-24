package com.epam.trainerworkload.dto;

public record MonthWorkloadResponse(
        int month,
        int trainingSummaryDuration
) {
}
