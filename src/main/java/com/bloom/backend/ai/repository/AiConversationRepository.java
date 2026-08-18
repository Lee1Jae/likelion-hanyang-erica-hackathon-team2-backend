package com.bloom.backend.ai.repository;
import com.bloom.backend.ai.domain.AiConversation;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    Optional<AiConversation> findByIdAndUserId(Long id, Long userId);
    List<AiConversation> findAllByUserIdOrderByLastMessageAtDesc(Long userId);
    void deleteAllByUserId(Long userId);
}
