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
import com.epam.trainerworkload.dto.MonthlyWorkloadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerWorkloadService {
    private final TrainerWorkloadRepository trainerWorkloadRepository;
    private final MonthlyWorkloadRepository monthlyWorkloadRepository;
    private final ProcessedWorkloadEventRepository processedWorkloadEventRepository;

    @Transactional
    public void updateWorkload(TrainerWorkloadRequest request) {
        String eventId = request.getEventId();

        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID is required");
        }

        if (processedWorkloadEventRepository.existsById(eventId)) {
            log.info("Workload event already processed: eventId={}", eventId);
            return;
        }

        ActionType actionType = request.getActionType();
        TrainerWorkload trainer;

        if (actionType == ActionType.ADD) {
            trainer = findOrCreateTrainer(request);
        } else if (actionType == ActionType.DELETE) {
            trainer = trainerWorkloadRepository
                    .findByUsername(request.getTrainerUsername())
                    .orElseThrow(() ->
                            new TrainerWorkloadNotFoundException(
                                    "Trainer workload not found: "
                                            + request.getTrainerUsername()
                            )
                    );
        } else {
            throw new IllegalArgumentException("Invalid action type");
        }

        updateTrainerDetails(trainer, request);

        if (actionType == ActionType.ADD) {
            MonthlyWorkload monthlyWorkload =
                    findOrCreateMonthlyWorkload(request, trainer);

            addDuration(
                    monthlyWorkload,
                    request.getTrainingDuration()
            );
        } else {
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

            deleteDuration(
                    monthlyWorkload,
                    request.getTrainingDuration()
            );
        }

        processedWorkloadEventRepository.save(
                new ProcessedWorkloadEvent(eventId)
        );

        log.info(
                "Workload updated successfully: eventId={}, trainer={}, action={}",
                eventId,
                request.getTrainerUsername(),
                actionType
        );
    }



    private TrainerWorkload findOrCreateTrainer(TrainerWorkloadRequest request) {
        return trainerWorkloadRepository
                .findByUsername(request.getTrainerUsername())
                .orElseGet(() -> {
                    TrainerWorkload trainer = new TrainerWorkload();

                    trainer.setUsername(request.getTrainerUsername());
                    trainer.setFirstName(request.getTrainerFirstName());
                    trainer.setLastName(request.getTrainerLastName());
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
    private MonthlyWorkload findOrCreateMonthlyWorkload(TrainerWorkloadRequest request, TrainerWorkload trainer) {
        return monthlyWorkloadRepository
                .findByTrainerAndYearAndMonth(
                        trainer,
                        request.getTrainingDate().getYear(),
                        request.getTrainingDate().getMonthValue())
                .orElseGet(() -> {
                    MonthlyWorkload monthlyWorkload = new MonthlyWorkload();

                    monthlyWorkload.setTrainer(trainer);
                    monthlyWorkload.setYear(request.getTrainingDate().getYear());
                    monthlyWorkload.setMonth(request.getTrainingDate().getMonthValue());
                    monthlyWorkload.setTrainingSummaryDuration(0);

                    return monthlyWorkloadRepository.save(monthlyWorkload);
                });
    }
    private void addDuration(MonthlyWorkload monthlyWorkload, Integer duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException(
                    "Training duration must be positive"
            );
        }
        monthlyWorkload.setTrainingSummaryDuration(monthlyWorkload.getTrainingSummaryDuration()+duration);
    }

    private void deleteDuration(MonthlyWorkload monthlyWorkload, Integer duration) {
        int updatedDuration =
                monthlyWorkload.getTrainingSummaryDuration() - duration;

        if (updatedDuration < 0) {
            throw new IllegalArgumentException(
                    "Training summary duration cannot be negative"
            );
        }

        monthlyWorkload.setTrainingSummaryDuration(updatedDuration);
    }

    @Transactional(readOnly = true)
    public MonthlyWorkloadResponse getMonthlyWorkload(
            String username,
            int year,
            int month
    ) {
        TrainerWorkload trainer = trainerWorkloadRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new TrainerWorkloadNotFoundException(
                                "Trainer workload not found: " + username
                        )
                );

        int duration = monthlyWorkloadRepository
                .findByTrainerAndYearAndMonth(trainer, year, month)
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
}
