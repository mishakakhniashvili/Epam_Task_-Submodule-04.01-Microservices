package com.epam.gymcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GymCrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymCrmApplication.class, args);
    }
}
