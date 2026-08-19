package com.bloom.backend.ai.dto;

import java.time.Instant;
import java.util.List;

public record MealRecommendationResponse(
        String title,
        String description,
        List<RecommendedFoodItem> foods,
        Integer totalKcal,
        Integer totalCarbs,
        Integer totalProtein,
        Integer totalFat,
        String reason,
        Instant generatedAt
) {}
