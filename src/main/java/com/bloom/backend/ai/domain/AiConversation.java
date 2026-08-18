package com.bloom.backend.ai.domain;

import com.bloom.backend.global.entity.BaseTimeEntity;
import com.bloom.backend.user.domain.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ai_conversations")
public class AiConversation extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false, length = 100)
    private String title;
    @Column(name = "last_message_at", nullable = false)
    private Instant lastMessageAt;
    protected AiConversation() {}
    public AiConversation(User user, String title, Instant now) { this.user = user; this.title = title; this.lastMessageAt = now; }
    public void touch(Instant now) { this.lastMessageAt = now; }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Instant getLastMessageAt() { return lastMessageAt; }
}
