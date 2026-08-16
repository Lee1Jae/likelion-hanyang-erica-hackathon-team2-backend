package com.bloom.backend.diary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyDiaryResponse(
        LocalDate date,
        BigDecimal weightKg,
        String mood,
        Integer stress,
        Integer fatigue,
        Integer waterMl,
        List<String> skin,
        LocalDate periodStart,
        LocalDate periodEnd,
        String note,
        int totalCalories,
        Integer calorieChange,
        int recommendedCalories,
        int remainingCalories,
        int totalActivity,
        Integer activityChange,
        List<MealResponse> meals,
        List<ActivityResponse> activities
) {}
