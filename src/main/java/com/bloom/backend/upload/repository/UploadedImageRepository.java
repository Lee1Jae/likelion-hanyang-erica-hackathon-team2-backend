package com.bloom.backend.upload.repository;

import com.bloom.backend.upload.domain.UploadedImage;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedImageRepository extends JpaRepository<UploadedImage, Long> {
    Optional<UploadedImage> findByIdAndUserId(Long id, Long userId);
    void deleteAllByUserId(Long userId);
    void deleteAllByExpiresAtBefore(Instant now);
}
