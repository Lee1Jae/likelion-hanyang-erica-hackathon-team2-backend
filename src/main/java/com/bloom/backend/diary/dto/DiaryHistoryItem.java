package com.bloom.backend.diary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DiaryHistoryItem(
        LocalDate date,
        BigDecimal weightKg,
        String mood,
        Integer waterMl,
        int totalCalories,
        int totalActivity
) {}
