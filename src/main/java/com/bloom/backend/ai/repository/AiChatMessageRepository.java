package com.bloom.backend.ai.repository;
import com.bloom.backend.ai.domain.AiChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {
    List<AiChatMessage> findAllByConversationIdOrderByCreatedAtAsc(Long conversationId);
    List<AiChatMessage> findTop20ByConversationIdOrderByCreatedAtDesc(Long conversationId);
    void deleteAllByConversationIdIn(List<Long> conversationIds);
}
