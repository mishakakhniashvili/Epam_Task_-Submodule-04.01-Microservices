package com.epam.trainerworkload.service;

import com.epam.trainerworkload.dto.ActionType;
import com.epam.trainerworkload.dto.TrainerWorkloadRequest;
import com.epam.trainerworkload.entity.MonthlyWorkload;
import com.epam.trainerworkload.entity.ProcessedWorkloadEvent;
import com.epam.trainerworkload.entity.TrainerWorkload;
import com.epam.trainerworkload.exception.MonthlyWorkloadNotFoundException;
import com.epam.trainerworkload.exception.TrainerWorkloadNotFoundException;
import com.epam.trainerworkload.repository.MonthlyWorkloadRepository;
import com.epam.trainerworkload.repository.ProcessedWorkloadEventRepository;
import com.epam.trainerworkload.repository.TrainerWorkloadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadTransactionExecutor {

    private final TrainerWorkloadRepository trainerWorkloadRepository;
    private final MonthlyWorkloadRepository monthlyWorkloadRepository;
    private final ProcessedWorkloadEventRepository processedEventRepository;

    @Transactional
    public void process(
            TrainerWorkloadRequest request,
            String eventId
    ) {
        if (processedEventRepository.existsById(eventId)) {
            log.info(
                    "Workload event already processed: eventId={}",
                    eventId
            );
            return;
        }

        ActionType actionType = request.getActionType();
        TrainerWorkload trainer = resolveTrainer(
                request,
                actionType
        );

        updateTrainerDetails(trainer, request);

        if (actionType == ActionType.ADD) {
            MonthlyWorkload monthlyWorkload =
                    findOrCreateMonthlyWorkload(request, trainer);

            addDuration(
                    monthlyWorkload,
                    request.getTrainingDuration()
            );
        } else if (actionType == ActionType.DELETE) {
            deleteDuration(request, trainer);
        } else {
            throw new IllegalArgumentException(
                    "Invalid action type"
            );
        }

        processedEventRepository.save(
                new ProcessedWorkloadEvent(eventId)
        );

        log.info(
                "Workload updated successfully: eventId={}, trainer={}, action={}",
                eventId,
                request.getTrainerUsername(),
                actionType
        );
    }

    private TrainerWorkload resolveTrainer(
            TrainerWorkloadRequest request,
            ActionType actionType
    ) {
        if (actionType == ActionType.ADD) {
            return findOrCreateTrainer(request);
        }

        if (actionType == ActionType.DELETE) {
            return trainerWorkloadRepository
                    .findByUsername(request.getTrainerUsername())
                    .orElseThrow(() ->
                            new TrainerWorkloadNotFoundException(
                                    "Trainer workload not found: "
                                            + request.getTrainerUsername()
                            )
                    );
        }

        throw new IllegalArgumentException(
                "Invalid action type"
        );
    }

    private TrainerWorkload findOrCreateTrainer(
            TrainerWorkloadRequest request
    ) {
        return trainerWorkloadRepository
                .findByUsername(request.getTrainerUsername())
                .orElseGet(() -> {
                    TrainerWorkload trainer =
                            new TrainerWorkload();

                    trainer.setUsername(
                            request.getTrainerUsername()
                    );
                    trainer.setFirstName(
                            request.getTrainerFirstName()
                    );
                    trainer.setLastName(
                            request.getTrainerLastName()
                    );
                    trainer.setActive(request.getActive());

                    return trainerWorkloadRepository.save(trainer);
                });
    }

    private void updateTrainerDetails(
            TrainerWorkload trainer,
            TrainerWorkloadRequest request
    ) {
        trainer.setFirstName(request.getTrainerFirstName());
        trainer.setLastName(request.getTrainerLastName());
        trainer.setActive(request.getActive());
    }

    private MonthlyWorkload findOrCreateMonthlyWorkload(
            TrainerWorkloadRequest request,
            TrainerWorkload trainer
    ) {
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        return monthlyWorkloadRepository
                .findByTrainerAndYearAndMonth(
                        trainer,
                        year,
                        month
                )
                .orElseGet(() -> {
                    MonthlyWorkload monthlyWorkload =
                            new MonthlyWorkload();

                    monthlyWorkload.setTrainer(trainer);
                    monthlyWorkload.setYear(year);
                    monthlyWorkload.setMonth(month);
                    monthlyWorkload.setTrainingSummaryDuration(0);

                    return monthlyWorkloadRepository.save(
                            monthlyWorkload
                    );
                });
    }

    private void addDuration(
            MonthlyWorkload monthlyWorkload,
            int duration
    ) {
        monthlyWorkload.setTrainingSummaryDuration(
                monthlyWorkload.getTrainingSummaryDuration()
                        + duration
        );
    }

    private void deleteDuration(
            TrainerWorkloadRequest request,
            TrainerWorkload trainer
    ) {
        int year = request.getTrainingDate().getYear();
        int month = request.getTrainingDate().getMonthValue();

        MonthlyWorkload monthlyWorkload =
                monthlyWorkloadRepository
                        .findByTrainerAndYearAndMonth(
                                trainer,
                                year,
                                month
                        )
                        .orElseThrow(() ->
                                new MonthlyWorkloadNotFoundException(
                                        "Monthly workload not found"
                                )
                        );

        int updatedDuration =
                monthlyWorkload.getTrainingSummaryDuration()
                        - request.getTrainingDuration();

        if (updatedDuration < 0) {
            throw new IllegalArgumentException(
                    "Training summary duration cannot be negative"
            );
        }

        monthlyWorkload.setTrainingSummaryDuration(
                updatedDuration
        );
    }
}
