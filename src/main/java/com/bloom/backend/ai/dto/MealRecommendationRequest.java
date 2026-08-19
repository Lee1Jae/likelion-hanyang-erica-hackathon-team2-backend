package com.bloom.backend.ai.dto;

import com.bloom.backend.diary.domain.MealType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MealRecommendationRequest(
        @NotNull LocalDate date,
        @NotNull MealType mealType
) {}
