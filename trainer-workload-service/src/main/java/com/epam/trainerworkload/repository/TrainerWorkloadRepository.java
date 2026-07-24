package com.epam.trainerworkload.repository;

import com.epam.trainerworkload.entity.TrainerWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkload, Long> {
    Optional<TrainerWorkload> findByUsername(String username);
}
