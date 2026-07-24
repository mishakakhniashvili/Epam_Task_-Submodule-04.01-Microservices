package com.epam.gymcrm.entity;

import com.epam.gymcrm.dto.workload.ActionType;
import com.epam.gymcrm.dto.workload.TrainerWorkloadRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "workload_outbox_events")
public class WorkloadOutboxEvent {

    @Id
    @Column(name = "event_id", length = 100, nullable = false)
    private String eventId;

    @Column(name = "transaction_id", length = 100, nullable = false)
    private String transactionId;

    @Column(name = "trainer_username", nullable = false)
    private String trainerUsername;

    @Column(name = "trainer_first_name", nullable = false)
    private String trainerFirstName;

    @Column(name = "trainer_last_name", nullable = false)
    private String trainerLastName;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "training_date", nullable = false)
    private LocalDate trainingDate;

    @Column(name = "training_duration", nullable = false)
    private int trainingDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 10, nullable = false)
    private ActionType actionType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(nullable = false)
    private int attempts;

    public static WorkloadOutboxEvent fromTraining(
            Training training,
            ActionType actionType,
            String transactionId,
            Instant createdAt
    ) {
        Trainer trainer = training.getTrainer();

        WorkloadOutboxEvent event = new WorkloadOutboxEvent();
        event.eventId = "training-"
                + training.getId()
                + "-"
                + actionType;
        event.transactionId = transactionId;
        event.trainerUsername = trainer.getUser().getUsername();
        event.trainerFirstName = trainer.getUser().getFirstName();
        event.trainerLastName = trainer.getUser().getLastName();
        event.active = trainer.getUser().isActive();
        event.trainingDate = training.getTrainingDate();
        event.trainingDuration = training.getTrainingDuration();
        event.actionType = actionType;
        event.createdAt = createdAt;
        event.nextAttemptAt = createdAt;
        event.attempts = 0;
        return event;
    }

    public TrainerWorkloadRequest toRequest() {
        return TrainerWorkloadRequest.builder()
                .eventId(eventId)
                .trainerUsername(trainerUsername)
                .trainerFirstName(trainerFirstName)
                .trainerLastName(trainerLastName)
                .active(active)
                .trainingDate(trainingDate)
                .trainingDuration(trainingDuration)
                .actionType(actionType)
                .build();
    }

    public void scheduleRetry(Instant now) {
        attempts++;
        long delaySeconds = Math.min(300L, 1L << Math.min(attempts, 8));
        nextAttemptAt = now.plusSeconds(delaySeconds);
    }
}
