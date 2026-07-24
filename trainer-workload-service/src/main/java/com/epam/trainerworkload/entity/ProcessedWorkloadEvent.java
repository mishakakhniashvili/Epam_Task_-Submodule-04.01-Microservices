package com.epam.trainerworkload.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "processed_workload_events")
public class ProcessedWorkloadEvent {

    @Id
    @Column(
            name = "event_id",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String eventId;

    public ProcessedWorkloadEvent(String eventId) {
        this.eventId = eventId;
    }
}