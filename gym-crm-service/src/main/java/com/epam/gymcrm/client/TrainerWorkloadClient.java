package com.epam.gymcrm.client;

import com.epam.gymcrm.dto.workload.TrainerWorkloadRequest;
import com.epam.gymcrm.exception.WorkloadServiceUnavailableException;
import com.epam.gymcrm.security.JwtService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class TrainerWorkloadClient {

    private static final String TRANSACTION_ID_HEADER =
            "X-Transaction-Id";

    private static final String TRANSACTION_ID_MDC_KEY =
            "transactionId";

    private final RestClient restClient;
    private final JwtService jwtService;

    public TrainerWorkloadClient(
            RestClient.Builder builder,
            @Value("${trainer-workload-service.base-url}") String baseUrl,
            JwtService jwtService
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
        this.jwtService = jwtService;
    }

    @Retry(
            name = "trainerWorkload",
            fallbackMethod = "updateWorkloadFallback"
    )
    @CircuitBreaker(name = "trainerWorkload")
    public void updateWorkload(TrainerWorkloadRequest request) {
        RestClient.RequestBodySpec requestSpec = restClient
                .post()
                .uri("/api/v1/workload-events")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + jwtService.generateWorkloadServiceToken()
                );

        String transactionId =
                MDC.get(TRANSACTION_ID_MDC_KEY);

        if (transactionId != null
                && !transactionId.isBlank()) {
            requestSpec.header(
                    TRANSACTION_ID_HEADER,
                    transactionId
            );
        }

        requestSpec
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private void updateWorkloadFallback(
            TrainerWorkloadRequest request,
            Throwable exception
    ) {
        log.error(
                "Could not update workload for trainer {} after retries: {}",
                request.getTrainerUsername(),
                exception.getMessage()
        );

        throw new WorkloadServiceUnavailableException(
                "Trainer workload service is unavailable",
                exception
        );
    }
}
