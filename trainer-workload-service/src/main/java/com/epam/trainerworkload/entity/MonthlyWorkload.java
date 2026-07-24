package com.epam.trainerworkload.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "monthly_workloads",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_monthly_workload_trainer_year_month",
                columnNames = {"trainer_id", "work_year", "work_month"}
        )
)
public class MonthlyWorkload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    @JoinColumn(name = "trainer_id", nullable = false)
    private TrainerWorkload trainer;

    @Column(name = "work_year", nullable = false)
    private int year;

    @Column(name = "work_month", nullable = false)
    private int month;

    @Column(nullable = false)
    private int trainingSummaryDuration;

    @Version
    private Long version;
}
