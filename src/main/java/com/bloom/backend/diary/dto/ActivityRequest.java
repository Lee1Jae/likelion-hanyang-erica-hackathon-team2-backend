package com.bloom.backend.diary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ActivityRequest(
        @Min(0) @Max(200000) int steps,
        @Min(0) @Max(1440) int exerciseMinutes,
        @Min(0) @Max(10000) int burnedKcal,
        @Size(max = 200) String memo
) {}
