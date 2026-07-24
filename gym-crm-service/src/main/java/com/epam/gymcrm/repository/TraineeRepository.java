package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.Trainee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    @EntityGraph(attributePaths = {
            "user",
            "trainers",
            "trainers.user",
            "trainers.specialization"
    }
    )
    Optional<Trainee> findByUserUsername(String username);
}