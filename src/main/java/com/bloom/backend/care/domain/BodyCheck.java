package com.bloom.backend.care.domain;

import com.bloom.backend.global.entity.BaseTimeEntity;
import com.bloom.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "body_checks")
public class BodyCheck extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;

    @Column(name = "original_image_url", nullable = false, length = 1000)
    private String originalImageUrl;

    @Column(name = "expected_image_url", length = 1000)
    private String expectedImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false, length = 20)
    private BodyCheckStatus analysisStatus;

    protected BodyCheck() {}

    public BodyCheck(User user, LocalDate recordedDate, String originalImageUrl) {
        this.user = user;
        this.recordedDate = recordedDate;
        this.originalImageUrl = originalImageUrl;
        this.analysisStatus = BodyCheckStatus.NOT_REQUESTED;
    }

    public void update(LocalDate recordedDate, String originalImageUrl) {
        if (recordedDate != null) this.recordedDate = recordedDate;
        if (originalImageUrl != null && !originalImageUrl.equals(this.originalImageUrl)) {
            this.originalImageUrl = originalImageUrl;
            this.expectedImageUrl = null;
            this.analysisStatus = BodyCheckStatus.NOT_REQUESTED;
        }
    }

    public void startAnalysis() {
        this.expectedImageUrl = null;
        this.analysisStatus = BodyCheckStatus.ANALYZING;
    }

    public void completeAnalysis(String expectedImageUrl) {
        this.expectedImageUrl = expectedImageUrl;
        this.analysisStatus = BodyCheckStatus.COMPLETED;
    }

    public void failAnalysis() {
        this.expectedImageUrl = null;
        this.analysisStatus = BodyCheckStatus.FAILED;
    }

    public Long getId() { return id; }
    public LocalDate getRecordedDate() { return recordedDate; }
    public String getOriginalImageUrl() { return originalImageUrl; }
    public String getExpectedImageUrl() { return expectedImageUrl; }
    public BodyCheckStatus getAnalysisStatus() { return analysisStatus; }
}
