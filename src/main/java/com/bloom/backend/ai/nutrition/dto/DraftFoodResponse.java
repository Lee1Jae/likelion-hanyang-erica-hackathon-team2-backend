package com.bloom.backend.ai.nutrition.dto;
import com.bloom.backend.ai.nutrition.domain.*;
import java.math.BigDecimal;
public record DraftFoodResponse(Long draftFoodId, String foodName, BigDecimal amount, String amountUnit,
        Integer kcal, Integer carbs, Integer protein, Integer fat, BigDecimal confidence, NutritionSource source) {
    public static DraftFoodResponse from(DraftFood food) { return new DraftFoodResponse(food.getId(), food.getFoodName(),
            food.getAmount(), food.getAmountUnit(), food.getKcal(), food.getCarbs(), food.getProtein(), food.getFat(),
            food.getConfidence(), food.getSource()); }
}
