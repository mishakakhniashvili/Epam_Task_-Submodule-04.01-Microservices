package com.epam.trainerworkload.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TransactionIdFilter extends OncePerRequestFilter {

    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    public static final String TRANSACTION_ID_MDC_KEY = "transactionId";
    private static final int MAX_TRANSACTION_ID_LENGTH = 100;
    private static final String VALID_TRANSACTION_ID = "[A-Za-z0-9._-]+";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String transactionId = resolveTransactionId(request);
        String endpoint = resolveEndpoint(request);

        long startTime = System.currentTimeMillis();
        String previousTransactionId =
                MDC.get(TRANSACTION_ID_MDC_KEY);

        MDC.put(TRANSACTION_ID_MDC_KEY, transactionId);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);

        try {
            log.info(
                    "Transaction started: method={}, endpoint={}",
                    request.getMethod(),
                    endpoint
            );

            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            log.info(
                    "Transaction completed: method={}, endpoint={}, status={}, duration={}ms",
                    request.getMethod(),
                    endpoint,
                    response.getStatus(),
                    duration
            );

            if (previousTransactionId == null) {
                MDC.remove(TRANSACTION_ID_MDC_KEY);
            } else {
                MDC.put(
                        TRANSACTION_ID_MDC_KEY,
                        previousTransactionId
                );
            }
        }
    }

    private String resolveTransactionId(HttpServletRequest request) {
        String transactionId =
                request.getHeader(TRANSACTION_ID_HEADER);

        if (transactionId == null
                || transactionId.isBlank()
                || transactionId.length() > MAX_TRANSACTION_ID_LENGTH
                || !transactionId.matches(VALID_TRANSACTION_ID)) {
            return UUID.randomUUID().toString();
        }

        return transactionId;
    }

    private String resolveEndpoint(HttpServletRequest request) {
        String queryString = request.getQueryString();

        if (queryString == null || queryString.isBlank()) {
            return request.getRequestURI();
        }

        return request.getRequestURI() + "?" + queryString;
    }
}
