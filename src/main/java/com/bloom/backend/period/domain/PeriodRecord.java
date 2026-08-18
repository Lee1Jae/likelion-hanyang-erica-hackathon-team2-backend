package com.bloom.backend.period.domain;

import com.bloom.backend.global.entity.BaseTimeEntity;
import com.bloom.backend.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "period_records", indexes = @Index(name = "idx_period_user_start", columnList = "user_id,start_date"))
public class PeriodRecord extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    protected PeriodRecord() {}

    public PeriodRecord(User user, LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void patch(LocalDate startDate, LocalDate endDate) {
        if (startDate != null) this.startDate = startDate;
        if (endDate != null) this.endDate = endDate;
    }

    public Long getId() { return id; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}
