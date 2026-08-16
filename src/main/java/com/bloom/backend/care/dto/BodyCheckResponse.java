package com.bloom.backend.care.dto;

import com.bloom.backend.care.domain.BodyCheck;
import com.bloom.backend.care.domain.BodyCheckStatus;
import java.time.Instant;
import java.time.LocalDate;

public record BodyCheckResponse(
        Long bodyCheckId,
        LocalDate recordedDate,
        String originalImageUrl,
        String expectedImageUrl,
        BodyCheckStatus analysisStatus,
        Instant createdAt,
        Instant updatedAt
) {
    public static BodyCheckResponse from(BodyCheck bodyCheck) {
        return new BodyCheckResponse(bodyCheck.getId(), bodyCheck.getRecordedDate(),
                bodyCheck.getOriginalImageUrl(), bodyCheck.getExpectedImageUrl(),
                bodyCheck.getAnalysisStatus(), bodyCheck.getCreatedAt(), bodyCheck.getUpdatedAt());
    }
}
