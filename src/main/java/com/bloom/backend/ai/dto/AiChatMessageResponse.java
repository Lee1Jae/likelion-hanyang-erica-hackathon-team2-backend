package com.bloom.backend.ai.dto;
import com.bloom.backend.ai.domain.ChatRole;
import java.time.Instant;
public record AiChatMessageResponse(ChatRole role, String content, Instant createdAt) {}
