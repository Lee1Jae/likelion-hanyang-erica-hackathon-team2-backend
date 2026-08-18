package com.bloom.backend.ai.nutrition.dto;
import com.bloom.backend.ai.nutrition.domain.NutritionAnalysisStatus;
import java.util.List;
public record NutritionAnalysisResponse(Long analysisId, NutritionAnalysisStatus status, String modelVersion,
        String imageUrl, List<DraftFoodResponse> foods, Integer totalKcal, boolean manualInputAvailable) {}
