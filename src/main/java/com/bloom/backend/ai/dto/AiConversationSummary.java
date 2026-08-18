package com.bloom.backend.ai.dto;
import java.time.Instant;
public record AiConversationSummary(Long conversationId, String title, Instant lastMessageAt) {}
