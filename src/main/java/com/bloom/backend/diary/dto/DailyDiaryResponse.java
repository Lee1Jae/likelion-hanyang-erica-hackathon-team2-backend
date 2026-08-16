package com.bloom.backend.diary.dto;

import com.bloom.backend.diary.domain.BodyConditionTag;
import com.bloom.backend.diary.domain.EmotionTag;
import com.bloom.backend.diary.domain.SkinTag;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyDiaryResponse(
        LocalDate date,
        BigDecimal weightKg,
        Integer emotionScore,
        Integer bodyScore,
        List<EmotionTag> emotionTags,
        List<BodyConditionTag> bodyTags,
        Integer waterMl,
        List<SkinTag> skin,
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
