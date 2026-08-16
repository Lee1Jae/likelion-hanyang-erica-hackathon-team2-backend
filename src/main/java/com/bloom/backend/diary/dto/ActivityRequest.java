package com.bloom.backend.diary.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ActivityRequest(
        @Min(0) @Max(200000) int activityAmount,
        @Size(max = 200) String memo
) {}
