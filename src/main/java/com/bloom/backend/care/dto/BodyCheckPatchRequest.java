package com.bloom.backend.care.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BodyCheckPatchRequest(
        LocalDate recordedDate,
        @Size(min = 1, max = 1000)
        @Pattern(regexp = "https?://.+", message = "http 또는 https 이미지 URL이어야 합니다.")
        String originalImageUrl
) {
    public boolean hasChanges() {
        return recordedDate != null || originalImageUrl != null;
    }
}
