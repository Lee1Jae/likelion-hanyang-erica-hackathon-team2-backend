package com.bloom.backend.diary.dto;

import com.bloom.backend.diary.domain.MealType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MealRequest(
        @NotNull MealType mealType,
        @NotBlank @Size(max = 100) String foodName,
        @Min(0) @Max(10000) int kcal,
        @Min(0) @Max(1000) int carbs,
        @Min(0) @Max(1000) int protein,
        @Min(0) @Max(1000) int fat
) {}
