package com.bloom.backend.ai.nutrition.dto;
import com.bloom.backend.ai.nutrition.domain.*;
import com.bloom.backend.diary.domain.NutritionValues;
import java.math.BigDecimal;
public record DraftFoodResponse(Long draftFoodId, String foodName, BigDecimal amount, String amountUnit,
        Integer kcal, Integer carbs, Integer protein, Integer fat, BigDecimal confidence, NutritionSource source) {
    public static DraftFoodResponse from(DraftFood food) {
        NutritionValues nutrition = NutritionValues.normalize(food.getKcal(), food.getCarbs(),
                food.getProtein(), food.getFat());
        return new DraftFoodResponse(food.getId(), food.getFoodName(), food.getAmount(), food.getAmountUnit(),
                nutrition.kcal(), nutrition.carbs(), nutrition.protein(), nutrition.fat(),
                food.getConfidence(), food.getSource());
    }
}
