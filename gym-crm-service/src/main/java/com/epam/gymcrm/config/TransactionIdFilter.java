package com.epam.gymcrm.config;

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

    private static final String HEADER_NAME = "X-Transaction-Id";
    private static final String MDC_KEY = "transactionId";
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

        long startedAt = System.currentTimeMillis();
        String previousTransactionId = MDC.get(MDC_KEY);

        MDC.put(MDC_KEY, transactionId);
        response.setHeader(HEADER_NAME, transactionId);

        try {
            log.info(
                    "Transaction started: method={}, endpoint={}",
                    request.getMethod(),
                    endpoint
            );

            filterChain.doFilter(request, response);
        } finally {
            log.info(
                    "Transaction completed: method={}, endpoint={}, status={}, duration={}ms",
                    request.getMethod(),
                    endpoint,
                    response.getStatus(),
                    System.currentTimeMillis() - startedAt
            );

            if (previousTransactionId == null) {
                MDC.remove(MDC_KEY);
            } else {
                MDC.put(MDC_KEY, previousTransactionId);
            }
        }
    }

    private String resolveTransactionId(HttpServletRequest request) {
        String transactionId = request.getHeader(HEADER_NAME);

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
