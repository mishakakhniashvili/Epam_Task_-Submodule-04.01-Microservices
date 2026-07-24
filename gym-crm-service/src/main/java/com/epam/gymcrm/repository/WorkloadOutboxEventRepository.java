package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.WorkloadOutboxEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WorkloadOutboxEventRepository
        extends JpaRepository<WorkloadOutboxEvent, String> {

    @Query("""
            select event.eventId
            from WorkloadOutboxEvent event
            where event.nextAttemptAt <= :now
            order by event.createdAt
            """)
    List<String> findReadyEventIds(
            @Param("now") Instant now,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select event
            from WorkloadOutboxEvent event
            where event.eventId = :eventId
            """)
    Optional<WorkloadOutboxEvent> findByIdForUpdate(
            @Param("eventId") String eventId
    );
}
