package com.bloom.backend.diary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DiaryResponse(
        LocalDate date,
        String memo,
        BigDecimal condition,
        BigDecimal conditionChange,
        BigDecimal weight,
        Integer waterIntake,
        String skinCondition,
        Boolean menstrualStatus,
        int totalCalories,
        Integer calorieChange,
        int recommendedCalories,
        int remainingCalories,
        int totalActivity,
        Integer activityChange,
        int carbs,
        int protein,
        int fat,
        List<MealResponse> meals,
        List<ActivityResponse> activities
) {}
