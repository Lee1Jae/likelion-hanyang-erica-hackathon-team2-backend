package com.bloom.backend.ai.domain;

import com.bloom.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "ai_chat_messages", indexes = @Index(name = "idx_chat_message_conversation", columnList = "conversation_id,created_at"))
public class AiChatMessage extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AiConversation conversation;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRole role;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AiMessageStatus status;
    protected AiChatMessage() {}
    public AiChatMessage(AiConversation conversation, ChatRole role, String content, AiMessageStatus status) {
        this.conversation = conversation; this.role = role; this.content = content; this.status = status;
    }
    public void complete() { this.status = AiMessageStatus.COMPLETED; }
    public void fail() { this.status = AiMessageStatus.FAILED; }
    public ChatRole getRole() { return role; }
    public String getContent() { return content; }
    public AiMessageStatus getStatus() { return status; }
}
