package com.bloom.backend.ai.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record AiChatRequest(Long conversationId, @NotBlank @Size(max = 2000) String message) {}
