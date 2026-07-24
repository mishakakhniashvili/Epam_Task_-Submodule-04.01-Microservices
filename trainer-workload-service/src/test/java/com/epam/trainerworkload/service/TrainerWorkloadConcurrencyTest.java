package com.epam.trainerworkload.service;

import com.epam.trainerworkload.dto.ActionType;
import com.epam.trainerworkload.dto.TrainerWorkloadRequest;
import com.epam.trainerworkload.dto.TrainerWorkloadResponse;
import com.epam.trainerworkload.repository.MonthlyWorkloadRepository;
import com.epam.trainerworkload.repository.ProcessedWorkloadEventRepository;
import com.epam.trainerworkload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TrainerWorkloadConcurrencyTest {

    @Autowired
    private TrainerWorkloadService trainerWorkloadService;

    @Autowired
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Autowired
    private MonthlyWorkloadRepository monthlyWorkloadRepository;

    @Autowired
    private ProcessedWorkloadEventRepository processedEventRepository;

    @BeforeEach
    @AfterEach
    void clearDatabase() {
        processedEventRepository.deleteAll();
        monthlyWorkloadRepository.deleteAll();
        trainerWorkloadRepository.deleteAll();
    }

    @Test
    void concurrentEventsForSameTrainerShouldNotLoseDuration()
            throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<?>> futures = List.of(
                    executor.submit(() -> {
                        awaitStart(ready, start);
                        trainerWorkloadService.updateWorkload(
                                request("event-1", 60)
                        );
                    }),
                    executor.submit(() -> {
                        awaitStart(ready, start);
                        trainerWorkloadService.updateWorkload(
                                request("event-2", 30)
                        );
                    })
            );

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }

            TrainerWorkloadResponse response =
                    trainerWorkloadService.getTrainerWorkload(
                            "john.smith",
                            2026,
                            7
                    );

            assertEquals(
                    90,
                    response.years()
                            .get(0)
                            .months()
                            .get(0)
                            .trainingSummaryDuration()
            );
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void concurrentDuplicateEventShouldBeAppliedOnce()
            throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            TrainerWorkloadRequest request =
                    request("same-event", 60);

            List<Future<?>> futures = List.of(
                    executor.submit(() -> {
                        awaitStart(ready, start);
                        trainerWorkloadService.updateWorkload(request);
                    }),
                    executor.submit(() -> {
                        awaitStart(ready, start);
                        trainerWorkloadService.updateWorkload(request);
                    })
            );

            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }

            assertEquals(
                    60,
                    trainerWorkloadService.getMonthlyWorkload(
                            "john.smith",
                            2026,
                            7
                    ).trainingSummaryDuration()
            );
        } finally {
            executor.shutdownNow();
        }
    }

    private void awaitStart(
            CountDownLatch ready,
            CountDownLatch start
    ) {
        ready.countDown();

        try {
            if (!start.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Timed out waiting to start"
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private TrainerWorkloadRequest request(
            String eventId,
            int duration
    ) {
        return TrainerWorkloadRequest.builder()
                .eventId(eventId)
                .trainerUsername("john.smith")
                .trainerFirstName("John")
                .trainerLastName("Smith")
                .active(true)
                .trainingDate(LocalDate.of(2026, 7, 20))
                .trainingDuration(duration)
                .actionType(ActionType.ADD)
                .build();
    }
}
