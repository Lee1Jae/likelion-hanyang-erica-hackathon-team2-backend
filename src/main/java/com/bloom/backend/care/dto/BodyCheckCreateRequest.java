package com.bloom.backend.care.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BodyCheckCreateRequest(
        @NotNull LocalDate recordedDate,
        @NotBlank
        @Size(max = 1000)
        @Pattern(regexp = "https?://.+", message = "http 또는 https 이미지 URL이어야 합니다.")
        String originalImageUrl
) {}
