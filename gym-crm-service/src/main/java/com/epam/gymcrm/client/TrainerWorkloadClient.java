package com.epam.gymcrm.client;

import com.epam.gymcrm.dto.workload.TrainerWorkloadRequest;
import com.epam.gymcrm.exception.WorkloadServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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

    public TrainerWorkloadClient(
            RestClient.Builder builder,
            @Value("${trainer-workload-service.base-url}") String baseUrl
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    @Retry(
            name = "trainerWorkload",
            fallbackMethod = "updateWorkloadFallback"
    )
    @CircuitBreaker(name = "trainerWorkload")
    public void updateWorkload(TrainerWorkloadRequest request) {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (!(authentication
                instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new IllegalStateException(
                    "JWT is unavailable for workload request"
            );
        }

        RestClient.RequestBodySpec requestSpec = restClient
                .post()
                .uri("/api/v1/workload-events")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer "
                                + jwtAuthentication
                                .getToken()
                                .getTokenValue()
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