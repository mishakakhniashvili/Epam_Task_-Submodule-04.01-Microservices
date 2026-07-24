package com.epam.gymcrm.config;

import com.epam.gymcrm.entity.TrainingType;
import com.epam.gymcrm.repository.TrainingTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Component
public class TrainingTypeInitializer implements ApplicationRunner {
    @Autowired
    TrainingTypeRepository trainingTypeRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {

        for(String name : List.of(
                "Fitness",
                "Yoga",
                "Zumba",
                "Stretching",
                "Resistance" )){
        if (trainingTypeRepository.findByName(name).isEmpty()) {
            trainingTypeRepository.save(new TrainingType(name));
        }
        }
    }
}
