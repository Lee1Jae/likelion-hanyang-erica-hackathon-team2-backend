package com.bloom.backend.upload.service;

import com.bloom.backend.upload.repository.UploadedImageRepository;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImageRetentionService {
    private final UploadedImageRepository imageRepository;

    public ImageRetentionService(UploadedImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Scheduled(cron = "${app.images.cleanup-cron:0 20 3 * * *}", zone = "Asia/Seoul")
    @Transactional
    public void deleteExpiredImages() {
        imageRepository.deleteAllByExpiresAtBefore(Instant.now());
    }
}
