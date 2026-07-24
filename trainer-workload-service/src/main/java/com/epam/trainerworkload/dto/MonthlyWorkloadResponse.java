package com.epam.trainerworkload.dto;

public record MonthlyWorkloadResponse(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        boolean active,
        int year,
        int month,
        int trainingSummaryDuration
) {
}