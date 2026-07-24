package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.Training;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Query("""
            select training
            from Training training
            where training.trainee.user.username = :traineeUsername
              and training.trainingDate >= coalesce(
                      :fromDate,
                      training.trainingDate
                  )
              and training.trainingDate <= coalesce(
                      :toDate,
                      training.trainingDate
                  )
              and training.trainer.user.username = coalesce(
                      :trainerUsername,
                      training.trainer.user.username
                  )
              and training.trainingType.trainingTypeName = coalesce(
                      :trainingTypeName,
                      training.trainingType.trainingTypeName
                  )
            """)
    @EntityGraph(attributePaths = {
            "trainer",
            "trainer.user",
            "trainee",
            "trainee.user",
            "trainingType"
    })
    List<Training> findTraineeTrainings(
            @Param("traineeUsername") String traineeUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("trainerUsername") String trainerUsername,
            @Param("trainingTypeName") String trainingTypeName
    );

    @Query("""
            select training
            from Training training
            where training.trainer.user.username = :trainerUsername
              and training.trainingDate >= coalesce(
                      :fromDate,
                      training.trainingDate
                  )
              and training.trainingDate <= coalesce(
                      :toDate,
                      training.trainingDate
                  )
              and training.trainee.user.username = coalesce(
                      :traineeUsername,
                      training.trainee.user.username
                  )
            """)
    @EntityGraph(attributePaths = {
            "trainer",
            "trainer.user",
            "trainee",
            "trainee.user",
            "trainingType"
    })
    List<Training> findTrainerTrainings(
            @Param("trainerUsername") String trainerUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("traineeUsername") String traineeUsername
    );

    List<Training> findAllByTraineeUserUsername(
            String traineeUsername
    );
}