package com.epam.trainerworkload.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Component
public class WorkloadUpdateLockManager {

    private static final int STRIPE_COUNT = 256;

    private final ReentrantLock[] locks =
            new ReentrantLock[STRIPE_COUNT];

    public WorkloadUpdateLockManager() {
        for (int index = 0; index < locks.length; index++) {
            locks[index] = new ReentrantLock();
        }
    }

    public void execute(
            String trainerUsername,
            String eventId,
            Runnable operation
    ) {
        int trainerIndex = stripeIndex(
                "trainer:" + trainerUsername
        );
        int eventIndex = stripeIndex("event:" + eventId);

        int firstIndex = Math.min(trainerIndex, eventIndex);
        int secondIndex = Math.max(trainerIndex, eventIndex);

        ReentrantLock firstLock = locks[firstIndex];
        ReentrantLock secondLock = locks[secondIndex];

        firstLock.lock();

        if (firstIndex != secondIndex) {
            secondLock.lock();
        }

        try {
            operation.run();
        } finally {
            if (firstIndex != secondIndex) {
                secondLock.unlock();
            }

            firstLock.unlock();
        }
    }

    private int stripeIndex(String value) {
        return Math.floorMod(value.hashCode(), locks.length);
    }
}
