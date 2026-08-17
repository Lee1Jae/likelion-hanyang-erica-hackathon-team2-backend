package com.bloom.backend.ai.dto;
import java.time.Instant;
import java.util.List;
public record ProcedureRecommendationsResponse(List<ProcedureRecommendationItem> recommendations, Instant generatedAt) {}
