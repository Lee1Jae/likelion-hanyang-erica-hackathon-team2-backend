package com.bloom.backend.ai.nutrition.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record DraftFoodRequest(@NotBlank @Size(max=100) String foodName,
        @PositiveOrZero BigDecimal amount, @Size(max=20) String amountUnit,
        @PositiveOrZero Integer kcal, @PositiveOrZero Integer carbs,
        @PositiveOrZero Integer protein, @PositiveOrZero Integer fat) {}
