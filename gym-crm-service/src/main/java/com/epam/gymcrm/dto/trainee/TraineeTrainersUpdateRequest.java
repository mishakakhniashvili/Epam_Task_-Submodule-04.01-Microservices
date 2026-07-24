package com.epam.gymcrm.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class TraineeTrainersUpdateRequest {

    @NotNull(message = "trainerUsernames is required")
    private List<
            @NotBlank(message = "trainer username cannot be blank")
                    String
            > trainerUsernames;
}