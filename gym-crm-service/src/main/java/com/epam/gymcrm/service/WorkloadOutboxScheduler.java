package com.epam.gymcrm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "workload.outbox.scheduling.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class WorkloadOutboxScheduler {

    private final WorkloadOutboxDispatcher dispatcher;

    @Value("${workload.outbox.batch-size:100}")
    private int batchSize;

    @Scheduled(
            fixedDelayString =
                    "${workload.outbox.dispatch-delay:1000}"
    )
    public void dispatchPendingEvents() {
        dispatcher.findReadyEventIds(batchSize)
                .forEach(dispatcher::dispatchEvent);
    }
}
