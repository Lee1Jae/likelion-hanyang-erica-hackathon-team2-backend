package com.bloom.backend.ai.dto;
import java.time.Instant;
public record AiChatResponse(Long conversationId, String answer, Instant createdAt) {}
