package com.bloom.backend.diary.dto;

import com.bloom.backend.diary.domain.Meal;
import com.bloom.backend.diary.domain.MealType;

public record MealResponse(Long mealId, MealType mealType, String foodName, Integer kcal,
                           Integer carbs, Integer protein, Integer fat) {
    public static MealResponse from(Meal meal) {
        return new MealResponse(meal.getId(), meal.getMealType(), meal.getFoodName(), meal.getCalories(),
                meal.getCarbs(), meal.getProtein(), meal.getFat());
    }
}
