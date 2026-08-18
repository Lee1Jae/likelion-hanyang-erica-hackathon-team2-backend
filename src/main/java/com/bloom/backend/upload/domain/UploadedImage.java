package com.bloom.backend.upload.domain;

import com.bloom.backend.global.entity.BaseTimeEntity;
import com.bloom.backend.user.domain.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "uploaded_images")
public class UploadedImage extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ImagePurpose purpose;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long size;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] data;

    @Column(name = "expires_at")
    private Instant expiresAt;

    protected UploadedImage() {}

    public UploadedImage(User user, ImagePurpose purpose, String contentType, long size, byte[] data,
                         Instant expiresAt) {
        this.user = user;
        this.purpose = purpose;
        this.contentType = contentType;
        this.size = size;
        this.data = data;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public String getContentType() { return contentType; }
    public long getSize() { return size; }
    public byte[] getData() { return data; }
    public Instant getExpiresAt() { return expiresAt; }
}
