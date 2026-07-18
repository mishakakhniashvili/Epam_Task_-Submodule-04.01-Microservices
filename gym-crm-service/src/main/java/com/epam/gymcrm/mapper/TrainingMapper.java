package com.epam.gymcrm.mapper;

import com.epam.gymcrm.dto.trainee.TraineeTrainingResponse;
import com.epam.gymcrm.dto.trainer.TrainerTrainingResponse;
import com.epam.gymcrm.entity.Training;
import com.epam.gymcrm.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper {
    public TraineeTrainingResponse toTraineeTrainingResponse(Training training){
        User trainerUser = training.getTrainer().getUser();
        String trainerName = trainerUser.getFirstName() + " " + trainerUser.getLastName();
        return new TraineeTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate().toString(),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDuration(),
                trainerName
                );
    }
    public TrainerTrainingResponse toTrainerTrainingResponse(Training training){
        User traineeUser = training.getTrainee().getUser();
        String traineeName = traineeUser.getFirstName() + " " + traineeUser.getLastName();
        return new TrainerTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate().toString(),
                training.getTrainingType().getTrainingTypeName(),
                training.getTrainingDuration(),
                traineeName
        );
    }

}
