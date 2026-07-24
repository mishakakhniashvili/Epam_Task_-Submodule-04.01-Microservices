package com.epam.trainerworkload.service;

import com.epam.trainerworkload.dto.MonthWorkloadResponse;
import com.epam.trainerworkload.dto.MonthlyWorkloadResponse;
import com.epam.trainerworkload.dto.TrainerWorkloadRequest;
import com.epam.trainerworkload.dto.TrainerWorkloadResponse;
import com.epam.trainerworkload.dto.YearWorkloadResponse;
import com.epam.trainerworkload.entity.MonthlyWorkload;
import com.epam.trainerworkload.entity.TrainerWorkload;
import com.epam.trainerworkload.exception.TrainerWorkloadNotFoundException;
import com.epam.trainerworkload.repository.MonthlyWorkloadRepository;
import com.epam.trainerworkload.repository.TrainerWorkloadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository trainerWorkloadRepository;
    private final MonthlyWorkloadRepository monthlyWorkloadRepository;
    private final TrainerWorkloadTransactionExecutor transactionExecutor;
    private final WorkloadUpdateLockManager lockManager;

    public void updateWorkload(TrainerWorkloadRequest request) {
        String eventId = resolveEventId(request.getEventId());

        lockManager.execute(
                request.getTrainerUsername(),
                eventId,
                () -> transactionExecutor.process(request, eventId)
        );
    }

    @Transactional(readOnly = true)
    public MonthlyWorkloadResponse getMonthlyWorkload(
            String username,
            int year,
            int month
    ) {
        TrainerWorkload trainer = findTrainer(username);

        int duration = monthlyWorkloadRepository
                .findByTrainerAndYearAndMonth(
                        trainer,
                        year,
                        month
                )
                .map(MonthlyWorkload::getTrainingSummaryDuration)
                .orElse(0);

        return new MonthlyWorkloadResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.isActive(),
                year,
                month,
                duration
        );
    }

    @Transactional(readOnly = true)
    public TrainerWorkloadResponse getTrainerWorkload(
            String username,
            Integer year,
            Integer month
    ) {
        if ((year == null) != (month == null)) {
            throw new IllegalArgumentException(
                    "Year and month must be provided together"
            );
        }

        TrainerWorkload trainer = findTrainer(username);
        List<MonthlyWorkload> workloads;

        if (year == null) {
            workloads = monthlyWorkloadRepository
                    .findAllByTrainerOrderByYearAscMonthAsc(
                            trainer
                    );
        } else {
            workloads = monthlyWorkloadRepository
                    .findByTrainerAndYearAndMonth(
                            trainer,
                            year,
                            month
                    )
                    .map(List::of)
                    .orElseGet(List::of);
        }

        List<YearWorkloadResponse> years =
                toYearResponses(workloads);

        if (year != null && years.isEmpty()) {
            years = List.of(
                    new YearWorkloadResponse(
                            year,
                            List.of(
                                    new MonthWorkloadResponse(
                                            month,
                                            0
                                    )
                            )
                    )
            );
        }

        return new TrainerWorkloadResponse(
                trainer.getUsername(),
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.isActive(),
                years
        );
    }

    private List<YearWorkloadResponse> toYearResponses(
            List<MonthlyWorkload> workloads
    ) {
        Map<Integer, List<MonthWorkloadResponse>> byYear =
                new LinkedHashMap<>();

        for (MonthlyWorkload workload : workloads) {
            byYear.computeIfAbsent(
                    workload.getYear(),
                    ignored -> new ArrayList<>()
            ).add(
                    new MonthWorkloadResponse(
                            workload.getMonth(),
                            workload.getTrainingSummaryDuration()
                    )
            );
        }

        return byYear.entrySet()
                .stream()
                .map(entry ->
                        new YearWorkloadResponse(
                                entry.getKey(),
                                List.copyOf(entry.getValue())
                        )
                )
                .toList();
    }

    private TrainerWorkload findTrainer(String username) {
        return trainerWorkloadRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new TrainerWorkloadNotFoundException(
                                "Trainer workload not found: "
                                        + username
                        )
                );
    }

    private String resolveEventId(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return eventId;
    }
}
