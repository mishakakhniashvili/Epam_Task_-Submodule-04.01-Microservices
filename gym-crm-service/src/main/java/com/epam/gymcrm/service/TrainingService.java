package com.epam.gymcrm.service;

import com.epam.gymcrm.client.TrainerWorkloadClient;
import com.epam.gymcrm.dto.workload.ActionType;
import com.epam.gymcrm.dto.workload.TrainerWorkloadRequest;
import com.epam.gymcrm.entity.Trainee;
import com.epam.gymcrm.entity.Trainer;
import com.epam.gymcrm.entity.Training;
import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.exception.EntityNotFoundException;
import com.epam.gymcrm.exception.ValidationException;
import com.epam.gymcrm.repository.TraineeRepository;
import com.epam.gymcrm.repository.TrainerRepository;
import com.epam.gymcrm.repository.TrainingRepository;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingService.class);

    private TrainingRepository trainingRepository;
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private TrainingTypeRepository trainingTypeRepository;
    private TrainerWorkloadClient trainerWorkloadClient;

    @Autowired
    public void setTrainingRepository(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Autowired
    public void setTrainerRepository(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Autowired
    public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Autowired
    public void setTrainerWorkloadClient(TrainerWorkloadClient trainerWorkloadClient) {
        this.trainerWorkloadClient = trainerWorkloadClient;
    }

    @Transactional
    public Training create(Training training) {
        Training createdTraining = trainingRepository.save(training);

        LOGGER.info("Created training with id={} and name={}",
                createdTraining.getId(),
                createdTraining.getTrainingName());

        return createdTraining;
    }

    @Transactional
    public Training addTraining(
            String trainerUsername,
            String traineeUsername,
            String trainingName,
            LocalDate trainingDate,
            Integer trainingDuration
    ) {
        validateParameter(trainerUsername);
        validateParameter(traineeUsername);
        validateParameter(trainingName);

        if (trainingDate == null) {
            throw new ValidationException("Training date is null");
        }

        if (trainingDuration == null) {
            throw new ValidationException("Training duration is null");
        }

        if (trainingDuration <= 0) {
            throw new ValidationException("Training duration is less or equal to 0");
        }



        Trainer trainer = trainerRepository.findByUserUsername(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException("trainer", trainerUsername));



        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("trainee", traineeUsername));

        TrainingType trainingType = trainer.getSpecialization();

        Training training = new Training(
                trainingName,
                trainingDate,
                trainer,
                trainee,
                trainingType,
                trainingDuration
        );

        Training createdTraining = create(training);

        TrainerWorkloadRequest workloadRequest =
                TrainerWorkloadRequest.builder()
                        .eventId(
                                "training-"
                                        + createdTraining.getId()
                                        + "-ADD"
                        )
                        .trainerUsername(
                                trainer.getUser().getUsername()
                        )
                        .trainerFirstName(
                                trainer.getUser().getFirstName()
                        )
                        .trainerLastName(
                                trainer.getUser().getLastName()
                        )
                        .active(trainer.getUser().isActive())
                        .trainingDate(
                                createdTraining.getTrainingDate()
                        )
                        .trainingDuration(
                                createdTraining.getTrainingDuration()
                        )
                        .actionType(ActionType.ADD)
                        .build();

        trainerWorkloadClient.updateWorkload(workloadRequest);

        return createdTraining;
    }

    public Optional<Training> findById(Long id) {
        LOGGER.info("Finding training with id={}", id);
        return trainingRepository.findById(id);
    }

    public List<Training> findAll() {
        return trainingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            String trainingTypeName
    ) {
        validateParameter(traineeUsername);

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ValidationException("From date cannot be after to date");
        }

        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("trainee", traineeUsername));

        return trainingRepository.findTraineeTrainings(
                traineeUsername,
                fromDate,
                toDate,
                trainerUsername,
                trainingTypeName
        );
    }

    private void validateParameter(String parameter) {
        if (parameter == null) {
            throw new ValidationException("Parameter should not be null");
        }

        if (parameter.isBlank()) {
            throw new ValidationException("Parameter should not be empty");
        }
    }

    @Transactional(readOnly = true)
    public List<TrainingType> getTrainingTypes() {
        return trainingTypeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername
    ) {
        validateParameter(trainerUsername);

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {
            throw new ValidationException(
                    "From date cannot be after to date"
            );
        }

        trainerRepository.findByUserUsername(trainerUsername)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "trainer",
                                trainerUsername
                        )
                );

        return trainingRepository.findTrainerTrainings(
                trainerUsername,
                fromDate,
                toDate,
                traineeUsername
        );
    }


    @Transactional(readOnly = true)
    public void deleteWorkloadsForTrainee(String traineeUsername) {
        List<Training> trainings =
                trainingRepository.findAllByTraineeUserUsername(traineeUsername);

        for (Training training : trainings) {
            Trainer trainer = training.getTrainer();

            TrainerWorkloadRequest request =
                    TrainerWorkloadRequest.builder()
                            .eventId(
                                    "training-"
                                            + training.getId()
                                            + "-DELETE"
                            )
                            .trainerUsername(
                                    trainer.getUser().getUsername()
                            )
                            .trainerFirstName(
                                    trainer.getUser().getFirstName()
                            )
                            .trainerLastName(
                                    trainer.getUser().getLastName()
                            )
                            .active(trainer.getUser().isActive())
                            .trainingDate(training.getTrainingDate())
                            .trainingDuration(
                                    training.getTrainingDuration()
                            )
                            .actionType(ActionType.DELETE)
                            .build();

            trainerWorkloadClient.updateWorkload(request);
        }
    }
}