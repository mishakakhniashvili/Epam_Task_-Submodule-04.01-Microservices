package com.epam.trainerworkload.config;

import com.epam.trainerworkload.controller.TrainerWorkloadController;
import com.epam.trainerworkload.service.TrainerWorkloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerWorkloadController.class)
@ContextConfiguration(classes = {
        TrainerWorkloadController.class,
        SecurityConfig.class
})
class TrainerWorkloadSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainerWorkloadService trainerWorkloadService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldReturnUnauthorizedWhenJwtIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/workload-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(trainerWorkloadService);
    }

    @Test
    void shouldReturnUnauthorizedWhenJwtIsInvalid() throws Exception {
        when(jwtDecoder.decode("invalid-token"))
                .thenThrow(new BadJwtException("Invalid JWT"));

        mockMvc.perform(post("/api/v1/workload-events")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(trainerWorkloadService);
    }

    @Test
    void shouldUpdateWorkloadWhenJwtIsValid() throws Exception {
        mockMvc.perform(post("/api/v1/workload-events")
                        .with(jwt().jwt(token -> token
                                .issuer("gym-crm")
                                .subject("john.smith")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isOk());

        verify(trainerWorkloadService).updateWorkload(any());
    }

    private String validRequestBody() {
        return """
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
    }
}