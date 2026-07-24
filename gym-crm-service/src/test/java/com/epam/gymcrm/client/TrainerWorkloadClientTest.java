package com.epam.gymcrm.client;

import com.epam.gymcrm.dto.workload.ActionType;
import com.epam.gymcrm.dto.workload.TrainerWorkloadRequest;
import com.epam.gymcrm.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TrainerWorkloadClientTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldSendServiceJwtAndTransactionId() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        JwtService jwtService = mock(JwtService.class);
        when(jwtService.generateWorkloadServiceToken())
                .thenReturn("service-token");

        TrainerWorkloadClient client =
                new TrainerWorkloadClient(
                        builder,
                        "http://trainer-workload-service",
                        jwtService
                );

        MDC.put("transactionId", "transaction-123");

        server.expect(
                        once(),
                        requestTo(
                                "http://trainer-workload-service"
                                        + "/api/v1/workload-events"
                        )
                )
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer service-token"
                ))
                .andExpect(header(
                        "X-Transaction-Id",
                        "transaction-123"
                ))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(
                        containsString("\"actionType\":\"ADD\"")
                ))
                .andRespond(withSuccess());

        client.updateWorkload(
                TrainerWorkloadRequest.builder()
                        .eventId("training-1-ADD")
                        .trainerUsername("john.smith")
                        .trainerFirstName("John")
                        .trainerLastName("Smith")
                        .active(true)
                        .trainingDate(LocalDate.of(2026, 7, 20))
                        .trainingDuration(60)
                        .actionType(ActionType.ADD)
                        .build()
        );

        server.verify();
    }
}
