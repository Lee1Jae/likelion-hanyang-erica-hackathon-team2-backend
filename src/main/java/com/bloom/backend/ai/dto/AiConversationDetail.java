package com.bloom.backend.ai.dto;
import java.util.List;
public record AiConversationDetail(Long conversationId, String title, List<AiChatMessageResponse> messages) {}
