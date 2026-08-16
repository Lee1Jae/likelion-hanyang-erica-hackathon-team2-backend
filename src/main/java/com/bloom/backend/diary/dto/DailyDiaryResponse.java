package com.bloom.backend.diary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyDiaryResponse(
        LocalDate date,
        BigDecimal weightKg,
        Integer emotionScore,
        Integer bodyScore,
        List<String> emotionTags,
        List<String> bodyTags,
        Integer waterMl,
        List<String> skin,
        LocalDate periodStart,
        LocalDate periodEnd,
        String memo,
        int totalCalories,
        Integer calorieChange,
        int recommendedCalories,
        int remainingCalories,
        int totalSteps,
        Integer stepsChange,
        int totalExerciseMinutes,
        Integer exerciseMinutesChange,
        int totalBurnedKcal,
        Integer burnedKcalChange,
        List<MealResponse> meals,
        List<ActivityResponse> activities
) {}
