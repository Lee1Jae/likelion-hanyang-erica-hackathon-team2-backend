package com.bloom.backend.ai.nutrition.dto;
import com.bloom.backend.ai.nutrition.domain.NutritionAnalysisStatus;
import com.bloom.backend.diary.dto.MealResponse;
import java.util.List;
public record NutritionRecordResponse(Long analysisId, NutritionAnalysisStatus status, List<MealResponse> meals) {}
