package com.bloom.backend.diary.dto;

import com.bloom.backend.diary.domain.BodyConditionTag;
import com.bloom.backend.diary.domain.EmotionTag;
import com.bloom.backend.diary.domain.SkinTag;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DiaryHistoryItem(
        LocalDate date,
        BigDecimal weightKg,
        Integer emotionScore,
        Integer bodyScore,
        Integer waterMl,
        int totalCalories,
        int totalSteps,
        int totalExerciseMinutes,
        int totalBurnedKcal,
        List<EmotionTag> emotionTags,
        List<BodyConditionTag> bodyTags,
        List<SkinTag> skin,
        LocalDate periodStart,
        LocalDate periodEnd
) {}
