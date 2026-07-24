package com.epam.trainerworkload.service;

import com.epam.trainerworkload.dto.ActionType;
import com.epam.trainerworkload.dto.TrainerWorkloadRequest;
import com.epam.trainerworkload.entity.MonthlyWorkload;
import com.epam.trainerworkload.entity.TrainerWorkload;
import com.epam.trainerworkload.exception.MonthlyWorkloadNotFoundException;
import com.epam.trainerworkload.exception.TrainerWorkloadNotFoundException;
import com.epam.trainerworkload.repository.MonthlyWorkloadRepository;
import com.epam.trainerworkload.repository.ProcessedWorkloadEventRepository;
import com.epam.trainerworkload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Mock
    private MonthlyWorkloadRepository monthlyWorkloadRepository;

    @Mock
    private ProcessedWorkloadEventRepository processedWorkloadEventRepository;

    private TrainerWorkloadTransactionExecutor transactionExecutor;

    @BeforeEach
    void setUp() {
        transactionExecutor = new TrainerWorkloadTransactionExecutor(
                trainerWorkloadRepository,
                monthlyWorkloadRepository,
                processedWorkloadEventRepository
        );
    }

    @Test
    void shouldCreateTrainerAndMonthlyWorkloadForAddAction() {
        TrainerWorkloadRequest request = createRequest(
                ActionType.ADD,
                60,
                LocalDate.of(2026, 7, 15)
        );

        when(trainerWorkloadRepository.findByUsername("john.smith"))
                .thenReturn(Optional.empty());

        when(trainerWorkloadRepository.save(any(TrainerWorkload.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(monthlyWorkloadRepository.findByTrainerAndYearAndMonth(
                any(TrainerWorkload.class), eq(2026), eq(7)
        )).thenReturn(Optional.empty());

        when(monthlyWorkloadRepository.save(any(MonthlyWorkload.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        transactionExecutor.process(request, "event-1");

        verify(trainerWorkloadRepository).save(
                argThat(trainer ->
                        trainer.getUsername().equals("john.smith")
                                && trainer.getFirstName().equals("John")
                                && trainer.getLastName().equals("Smith")
                                && trainer.isActive()
                )
        );

        verify(monthlyWorkloadRepository).save(
                argThat(workload ->
                        workload.getYear() == 2026
                                && workload.getMonth() == 7
                                && workload.getTrainingSummaryDuration() == 60
                )
        );
    }

    @Test
    void shouldAddDurationToExistingMonthlyWorkload() {
        TrainerWorkloadRequest request = createRequest(
                ActionType.ADD,
                30,
                LocalDate.of(2026, 7, 20)
        );

        TrainerWorkload trainer = createTrainer();
        MonthlyWorkload monthlyWorkload =
                createMonthlyWorkload(trainer, 2026, 7, 60);

        when(trainerWorkloadRepository.findByUsername("john.smith"))
                .thenReturn(Optional.of(trainer));

        when(monthlyWorkloadRepository.findByTrainerAndYearAndMonth(
                trainer, 2026, 7
        )).thenReturn(Optional.of(monthlyWorkload));

        transactionExecutor.process(request, "event-1");

        assertEquals(90, monthlyWorkload.getTrainingSummaryDuration());
        assertEquals("John", trainer.getFirstName());
        assertEquals("Smith", trainer.getLastName());
        assertTrue(trainer.isActive());

        verify(trainerWorkloadRepository, never()).save(any());
        verify(monthlyWorkloadRepository, never()).save(any());
    }

    @Test
    void shouldSubtractDurationForDeleteAction() {
        TrainerWorkloadRequest request = createRequest(
                ActionType.DELETE,
                30,
                LocalDate.of(2026, 7, 20)
        );

        TrainerWorkload trainer = createTrainer();
        MonthlyWorkload monthlyWorkload =
                createMonthlyWorkload(trainer, 2026, 7, 90);

        when(trainerWorkloadRepository.findByUsername("john.smith"))
                .thenReturn(Optional.of(trainer));

        when(monthlyWorkloadRepository.findByTrainerAndYearAndMonth(
                trainer, 2026, 7
        )).thenReturn(Optional.of(monthlyWorkload));

        transactionExecutor.process(request, "event-1");

        assertEquals(60, monthlyWorkload.getTrainingSummaryDuration());
    }

    @Test
    void shouldAllowDurationToBecomeZeroForDeleteAction() {
        TrainerWorkloadRequest request = createRequest(
                ActionType.DELETE,
                60,
                LocalDate.of(2026, 7, 20)
        );

        TrainerWorkload trainer = createTrainer();
        MonthlyWorkload monthlyWorkload =
                createMonthlyWorkload(trainer, 2026, 7, 60);

        when(trainerWorkloadRepository.findByUsername("john.smith"))
                .thenReturn(Optional.of(trainer));

        when(monthlyWorkloadRepository.findByTrainerAndYearAndMonth(
                trainer, 2026, 7
        )).thenReturn(Optional.of(monthlyWorkload));

        transactionExecutor.process(request, "event-1");

        assertEquals(0, monthlyWorkload.getTrainingSummaryDuration());
    }

    @Test
    void shouldThrowExceptionWhenDeleteWouldCreateNegativeDuration() {
        TrainerWorkloadRequest request = createRequest(
                ActionType.DELETE,
                90,
                LocalDate.of(2026, 7, 20)
        );

        TrainerWorkload trainer = createTrainer();
        MonthlyWorkload monthlyWorkload =
                createMonthlyWorkload(trainer, 2026, 7, 60);

        when(trainerWorkloadRepository.findByUsername("john.smith"))
                .thenReturn(Optional.of(trainer));

        when(monthlyWorkloadRepository.findByTrainerAndYearAndMonth(
                trainer, 2026, 7
        )).thenReturn(Optional.of(monthlyWorkload));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionExecutor.process(request, "event-1")
        );

        assertEquals(
                "Training summary duration cannot be negative",
                exception.getMessage()
        );

        assertEquals(60, monthlyWorkload.getTrainingSummaryDuration());
    }

    @Test
    void shouldThrowExceptionWhenTrainerDoesNotExistForDeleteAction() {
        TrainerWorkloadRequest request = createRequest(
                ActionType.DELETE,
                30,
                LocalDate.of(2026, 7, 20)
        );

        when(trainerWorkloadRepository.findByUsername("john.smith"))
                .thenReturn(Optional.empty());

        assertThrows(
                TrainerWorkloadNotFoundException.class,
                () -> transactionExecutor.process(request, "event-1")
        );

        verifyNoInteractions(monthlyWorkloadRepository);
    }

    @Test
    void shouldThrowExceptionWhenMonthlyWorkloadDoesNotExistForDeleteAction() {
        TrainerWorkloadRequest request = createRequest(
                ActionType.DELETE,
                30,
                LocalDate.of(2026, 7, 20)
        );

        TrainerWorkload trainer = createTrainer();

        when(trainerWorkloadRepository.findByUsername("john.smith"))
                .thenReturn(Optional.of(trainer));

        when(monthlyWorkloadRepository.findByTrainerAndYearAndMonth(
                trainer, 2026, 7
        )).thenReturn(Optional.empty());

        assertThrows(
                MonthlyWorkloadNotFoundException.class,
                () -> transactionExecutor.process(request, "event-1")
        );
    }

    @Test
    void shouldIgnoreAlreadyProcessedEvent() {
        TrainerWorkloadRequest request =
                mock(TrainerWorkloadRequest.class);

        when(processedWorkloadEventRepository.existsById("event-1"))
                .thenReturn(true);

        transactionExecutor.process(request, "event-1");

        verify(processedWorkloadEventRepository)
                .existsById("event-1");

        verifyNoInteractions(
                trainerWorkloadRepository,
                monthlyWorkloadRepository
        );

        verify(
                processedWorkloadEventRepository,
                never()
        ).save(any());
    }
    private TrainerWorkloadRequest createRequest(
            ActionType actionType,
            int duration,
            LocalDate date
    ) {
        TrainerWorkloadRequest request = mock(TrainerWorkloadRequest.class);

        when(request.getTrainerUsername()).thenReturn("john.smith");
        lenient().when(request.getTrainerFirstName()).thenReturn("John");
        lenient().when(request.getTrainerLastName()).thenReturn("Smith");
        lenient().when(request.getActive()).thenReturn(true);
        lenient().when(request.getTrainingDate()).thenReturn(date);
        lenient().when(request.getTrainingDuration()).thenReturn(duration);
        when(request.getActionType()).thenReturn(actionType);
        return request;
    }

    private TrainerWorkload createTrainer() {
        TrainerWorkload trainer = new TrainerWorkload();
        trainer.setUsername("john.smith");
        trainer.setFirstName("Old");
        trainer.setLastName("Name");
        trainer.setActive(false);
        return trainer;
    }

    private MonthlyWorkload createMonthlyWorkload(
            TrainerWorkload trainer,
            int year,
            int month,
            int duration
    ) {
        MonthlyWorkload workload = new MonthlyWorkload();
        workload.setTrainer(trainer);
        workload.setYear(year);
        workload.setMonth(month);
        workload.setTrainingSummaryDuration(duration);
        return workload;
    }
}
