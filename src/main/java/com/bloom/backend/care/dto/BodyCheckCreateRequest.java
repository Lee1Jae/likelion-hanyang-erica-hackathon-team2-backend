package com.bloom.backend.care.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BodyCheckCreateRequest(
        @NotNull LocalDate recordedDate,
        @NotBlank @Size(max = 1000) String originalImageUrl
) {}
