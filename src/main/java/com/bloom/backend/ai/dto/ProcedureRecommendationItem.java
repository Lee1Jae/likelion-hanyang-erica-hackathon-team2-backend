package com.bloom.backend.ai.dto;
public record ProcedureRecommendationItem(String procedureId, String name, String description, String reason,
        String estimatedSessions, String interval, Integer estimatedPrice) {}
