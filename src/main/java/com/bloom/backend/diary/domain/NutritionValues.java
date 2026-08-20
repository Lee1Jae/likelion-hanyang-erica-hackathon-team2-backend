package com.bloom.backend.diary.domain;

/**
 * Normalized nutrition values used at API and persistence boundaries.
 * Unknown values stay null; zero is reserved for a known zero.
 */
public record NutritionValues(Integer kcal, Integer carbs, Integer protein, Integer fat) {

    public static NutritionValues normalize(Integer kcal, Integer carbs, Integer protein, Integer fat) {
        Integer normalizedKcal = kcal;
        Integer normalizedCarbs = carbs;
        Integer normalizedProtein = protein;
        Integer normalizedFat = fat;

        boolean anyPositiveMacro = positive(carbs) || positive(protein) || positive(fat);
        boolean allMacrosKnown = carbs != null && protein != null && fat != null;

        if ((kcal == null || kcal == 0) && anyPositiveMacro) {
            normalizedKcal = allMacrosKnown
                    ? carbs * 4 + protein * 4 + fat * 9
                    : null;
        }

        boolean hasKnownMacro = carbs != null || protein != null || fat != null;
        if (positive(normalizedKcal) && hasKnownMacro && !anyPositiveMacro) {
            normalizedCarbs = null;
            normalizedProtein = null;
            normalizedFat = null;
        }

        return new NutritionValues(normalizedKcal, normalizedCarbs, normalizedProtein, normalizedFat);
    }

    public boolean incomplete() {
        return kcal == null || carbs == null || protein == null || fat == null;
    }

    private static boolean positive(Integer value) {
        return value != null && value > 0;
    }
}
