package com.bloom.backend.diary.dto;

import com.bloom.backend.diary.domain.Meal;
import com.bloom.backend.diary.domain.MealType;
import com.bloom.backend.diary.domain.NutritionValues;

public record MealResponse(Long mealId, MealType mealType, String foodName, Integer kcal,
                           Integer carbs, Integer protein, Integer fat,
                           Long nutritionAnalysisId, String sourceImageUrl) {
    public static MealResponse from(Meal meal) {
        NutritionValues nutrition = NutritionValues.normalize(meal.getCalories(), meal.getCarbs(),
                meal.getProtein(), meal.getFat());
        return new MealResponse(meal.getId(), meal.getMealType(), meal.getFoodName(), nutrition.kcal(),
                nutrition.carbs(), nutrition.protein(), nutrition.fat(), meal.getNutritionAnalysisId(),
                meal.getSourceImageUrl());
    }
}
