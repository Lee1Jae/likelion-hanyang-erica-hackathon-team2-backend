package com.bloom.backend.ai.service;

import com.bloom.backend.ai.client.OpenAiResponsesClient;
import com.bloom.backend.ai.domain.*;
import com.bloom.backend.ai.dto.*;
import com.bloom.backend.ai.repository.*;
import com.bloom.backend.global.error.*;
import com.bloom.backend.user.domain.User;
import com.bloom.backend.user.repository.UserRepository;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final String INSTRUCTIONS = """
            당신은 산후 건강·바디케어 앱 BLOOM의 생활관리 도우미다. 의료 진단이나 처방을 하지 말고,
            응급·위험 증상이 의심되면 의료기관 상담을 권한다. 제공된 사용자 기록만 근거로 사용하며
            누락값을 추측하지 않는다. 친절한 한국어로 짧고 실행 가능한 답변을 제공한다.
            """;
    private final AiConversationRepository conversationRepository;
    private final AiChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final AiContextService contextService;
    private final OpenAiResponsesClient aiClient;

    public AiChatService(AiConversationRepository conversationRepository, AiChatMessageRepository messageRepository,
                         UserRepository userRepository, AiContextService contextService, OpenAiResponsesClient aiClient) {
        this.conversationRepository = conversationRepository; this.messageRepository = messageRepository;
        this.userRepository = userRepository; this.contextService = contextService; this.aiClient = aiClient;
    }

    public AiChatResponse chat(Long userId, AiChatRequest request) {
        Instant now = Instant.now();
        AiConversation conversation = request.conversationId() == null
                ? createConversation(userId, request.message(), now) : findConversation(userId, request.conversationId());
        AiChatMessage userMessage = messageRepository.save(
                new AiChatMessage(conversation, ChatRole.USER, request.message().trim(), AiMessageStatus.PENDING));
        try {
            LocalDate today = LocalDate.now(SEOUL);
            String context = contextService.context(userId, today.minusDays(13), today);
            List<Map<String, Object>> input = new ArrayList<>();
            input.add(Map.of("role", "developer", "content", "사용자 개인화 데이터(JSON): " + context));
            List<AiChatMessage> history = messageRepository.findTop20ByConversationIdOrderByCreatedAtDesc(conversation.getId());
            Collections.reverse(history);
            history.stream().filter(message -> message.getStatus() != AiMessageStatus.FAILED).forEach(message ->
                    input.add(Map.of("role", message.getRole() == ChatRole.USER ? "user" : "assistant",
                            "content", message.getContent())));
            String answer = aiClient.text(INSTRUCTIONS, input);
            userMessage.complete(); messageRepository.save(userMessage);
            AiChatMessage assistant = messageRepository.save(
                    new AiChatMessage(conversation, ChatRole.ASSISTANT, answer, AiMessageStatus.COMPLETED));
            conversation.touch(Instant.now()); conversationRepository.save(conversation);
            return new AiChatResponse(conversation.getId(), answer, assistant.getCreatedAt());
        } catch (BusinessException exception) {
            userMessage.fail(); messageRepository.save(userMessage); throw exception;
        }
    }

    public List<AiConversationSummary> conversations(Long userId) {
        return conversationRepository.findAllByUserIdOrderByLastMessageAtDesc(userId).stream()
                .map(value -> new AiConversationSummary(value.getId(), value.getTitle(), value.getLastMessageAt())).toList();
    }

    public AiConversationDetail conversation(Long userId, Long conversationId) {
        AiConversation conversation = findConversation(userId, conversationId);
        List<AiChatMessageResponse> messages = messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream().filter(message -> message.getStatus() == AiMessageStatus.COMPLETED)
                .map(message -> new AiChatMessageResponse(message.getRole(), message.getContent(), message.getCreatedAt())).toList();
        return new AiConversationDetail(conversation.getId(), conversation.getTitle(), messages);
    }

    private AiConversation createConversation(Long userId, String message, Instant now) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String title = message.trim().length() <= 30 ? message.trim() : message.trim().substring(0, 30);
        return conversationRepository.save(new AiConversation(user, title, now));
    }
    private AiConversation findConversation(Long userId, Long id) {
        return conversationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_CONVERSATION_NOT_FOUND));
    }
}
