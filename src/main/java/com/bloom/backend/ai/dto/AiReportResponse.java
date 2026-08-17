package com.bloom.backend.ai.dto;
import java.time.*;
import java.util.List;
public record AiReportResponse(Long reportId, LocalDate from, LocalDate to, AiReportStatus status,
        String summary, List<AiReportItem> priorities, List<AiReportItem> methods, Instant generatedAt) {}
