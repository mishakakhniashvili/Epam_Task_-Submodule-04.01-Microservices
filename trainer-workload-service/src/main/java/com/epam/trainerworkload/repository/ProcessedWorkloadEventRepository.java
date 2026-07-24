package com.epam.trainerworkload.repository;

import com.epam.trainerworkload.entity.ProcessedWorkloadEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedWorkloadEventRepository
        extends JpaRepository<ProcessedWorkloadEvent, String> {
}