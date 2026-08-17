package com.bloom.backend.ai.dto;

import jakarta.validation.constraints.NotNull;
public record ProcedureRecommendationRequest(@NotNull Long bodyCheckId) {}
