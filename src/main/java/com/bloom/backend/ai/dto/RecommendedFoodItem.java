package com.bloom.backend.ai.dto;

import java.math.BigDecimal;

public record RecommendedFoodItem(
        String foodName,
        BigDecimal amount,
        String amountUnit,
        Integer kcal,
        Integer carbs,
        Integer protein,
        Integer fat
) {}
