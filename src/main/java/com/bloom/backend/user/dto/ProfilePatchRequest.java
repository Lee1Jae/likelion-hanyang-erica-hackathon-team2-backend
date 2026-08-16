package com.bloom.backend.user.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProfilePatchRequest(
        @DecimalMin("100.0") @DecimalMax("220.0") BigDecimal heightCm,
        @DecimalMin("20.0") @DecimalMax("300.0") BigDecimal weightKg,
        List<String> beautyGoals,
        List<String> healthIssues,
        LocalDate lastPeriodDate,
        @Positive Integer cycleLength
) {}
