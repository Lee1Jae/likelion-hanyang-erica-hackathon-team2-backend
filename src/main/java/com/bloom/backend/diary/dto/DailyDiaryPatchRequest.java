package com.bloom.backend.diary.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyDiaryPatchRequest(
        @NotNull LocalDate date,
        @DecimalMin("20.0") @DecimalMax("300.0") @Digits(integer = 3, fraction = 1) BigDecimal weightKg,
        @Size(max = 30) String mood,
        @Min(0) @Max(10) Integer stress,
        @Min(0) @Max(10) Integer fatigue,
        @Min(0) @Max(10000) Integer waterMl,
        @Size(max = 10) List<@Size(max = 30) String> skin,
        LocalDate periodStart,
        LocalDate periodEnd,
        @Size(max = 1000) String note
) {}
