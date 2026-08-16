package com.bloom.backend.diary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DiaryHistoryItem(
        LocalDate date,
        BigDecimal weightKg,
        Integer emotionScore,
        Integer bodyScore,
        Integer waterMl,
        int totalCalories,
        int totalSteps,
        int totalExerciseMinutes,
        int totalBurnedKcal
) {}
