package com.bloom.backend.user.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProfileResponse(
        Long userId,
        String email,
        String nickname,
        LocalDate birthDate,
        LocalDate deliveryDate,
        BigDecimal heightCm,
        BigDecimal weightKg,
        List<String> beautyGoals,
        List<String> healthIssues,
        LocalDate lastPeriodDate,
        Integer cycleLength,
        boolean onboardingCompleted
) {}
