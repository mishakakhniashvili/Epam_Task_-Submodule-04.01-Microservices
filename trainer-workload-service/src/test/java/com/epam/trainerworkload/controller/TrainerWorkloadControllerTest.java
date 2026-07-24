package com.epam.trainerworkload.controller;

import com.epam.trainerworkload.exception.GlobalExceptionHandler;
import com.epam.trainerworkload.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadControllerTest {

    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TrainerWorkloadController controller =
                new TrainerWorkloadController(trainerWorkloadService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldUpdateWorkloadAndReturnNoContent() throws Exception {
        String requestBody = """
                {
                  "eventId": "event-123",
                  "trainerUsername": "john.smith",
                  "trainerFirstName": "John",
                  "trainerLastName": "Smith",
                  "active": true,
                  "trainingDate": "2026-07-20",
                  "trainingDuration": 60,
                  "actionType": "ADD"
                }
                """;

        mockMvc.perform(post("/api/v1/workload-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(trainerWorkloadService).updateWorkload(any());
    }

    @Test
    void shouldReturnBadRequestWhenUsernameIsBlank() throws Exception {
        String requestBody = """
                {
                  "eventId": "event-124",
                  "trainerUsername": "",
                  "trainerFirstName": "John",
                  "trainerLastName": "Smith",
                  "active": true,
                  "trainingDate": "2026-07-20",
                  "trainingDuration": 60,
                  "actionType": "ADD"
                }
                """;

        mockMvc.perform(post("/api/v1/workload-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(trainerWorkloadService, never()).updateWorkload(any());
    }

    @Test
    void shouldReturnBadRequestWhenDurationIsNotPositive() throws Exception {
        String requestBody = """
                {
                  "eventId": "event-125",
                  "trainerUsername": "john.smith",
                  "trainerFirstName": "John",
                  "trainerLastName": "Smith",
                  "active": true,
                  "trainingDate": "2026-07-20",
                  "trainingDuration": 0,
                  "actionType": "ADD"
                }
                """;

        mockMvc.perform(post("/api/v1/workload-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(trainerWorkloadService, never()).updateWorkload(any());
    }

    @Test
    void shouldReturnBadRequestWhenActionTypeIsMissing() throws Exception {
        String requestBody = """
                {
                  "eventId": "event-126",
                  "trainerUsername": "john.smith",
                  "trainerFirstName": "John",
                  "trainerLastName": "Smith",
                  "active": true,
                  "trainingDate": "2026-07-20",
                  "trainingDuration": 60
                }
                """;

        mockMvc.perform(post("/api/v1/workload-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(trainerWorkloadService, never()).updateWorkload(any());
    }

    @Test
    void shouldReturnBadRequestForInvalidActionType() throws Exception {
        String requestBody = """
                {
                  "eventId": "event-127",
                  "trainerUsername": "john.smith",
                  "trainerFirstName": "John",
                  "trainerLastName": "Smith",
                  "active": true,
                  "trainingDate": "2026-07-20",
                  "trainingDuration": 60,
                  "actionType": "UPDATE"
                }
                """;

        mockMvc.perform(post("/api/v1/workload-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(trainerWorkloadService, never()).updateWorkload(any());
    }

    @Test
    void shouldReturnBadRequestWhenEventIdIsBlank() throws Exception {
        String requestBody = """
                {
                  "eventId": "",
                  "trainerUsername": "john.smith",
                  "trainerFirstName": "John",
                  "trainerLastName": "Smith",
                  "active": true,
                  "trainingDate": "2026-07-20",
                  "trainingDuration": 60,
                  "actionType": "ADD"
                }
                """;

        mockMvc.perform(post("/api/v1/workload-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(trainerWorkloadService, never()).updateWorkload(any());
    }
}