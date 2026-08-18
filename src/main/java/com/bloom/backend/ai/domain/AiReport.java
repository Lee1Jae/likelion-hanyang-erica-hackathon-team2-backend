package com.bloom.backend.ai.domain;

import com.bloom.backend.ai.dto.AiReportStatus;
import com.bloom.backend.global.entity.BaseTimeEntity;
import com.bloom.backend.user.domain.User;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ai_reports", indexes = @Index(name = "idx_ai_report_user_created", columnList = "user_id,created_at"))
public class AiReport extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;
    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiReportStatus status;
    @Column(columnDefinition = "TEXT")
    private String summary;
    @Column(name = "priorities_json", columnDefinition = "TEXT")
    private String prioritiesJson;
    @Column(name = "methods_json", columnDefinition = "TEXT")
    private String methodsJson;
    @Column(name = "model_version", length = 100)
    private String modelVersion;

    protected AiReport() {}
    public AiReport(User user, LocalDate fromDate, LocalDate toDate) {
        this.user = user; this.fromDate = fromDate; this.toDate = toDate; this.status = AiReportStatus.PROCESSING;
    }
    public void complete(String summary, String prioritiesJson, String methodsJson, String modelVersion) {
        this.summary = summary; this.prioritiesJson = prioritiesJson; this.methodsJson = methodsJson;
        this.modelVersion = modelVersion; this.status = AiReportStatus.COMPLETED;
    }
    public void fail() { this.status = AiReportStatus.FAILED; }
    public Long getId() { return id; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public AiReportStatus getStatus() { return status; }
    public String getSummary() { return summary; }
    public String getPrioritiesJson() { return prioritiesJson; }
    public String getMethodsJson() { return methodsJson; }
}
