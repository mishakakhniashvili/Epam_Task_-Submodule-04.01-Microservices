package com.epam.trainerworkload.repository;

import com.epam.trainerworkload.entity.MonthlyWorkload;
import com.epam.trainerworkload.entity.TrainerWorkload;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface MonthlyWorkloadRepository extends JpaRepository<MonthlyWorkload, Long> {
    Optional<MonthlyWorkload> findByTrainerAndYearAndMonth(
            TrainerWorkload trainer,
            int year,
            int month
    );

    List<MonthlyWorkload> findAllByTrainerOrderByYearAscMonthAsc(
            TrainerWorkload trainer
    );
}
