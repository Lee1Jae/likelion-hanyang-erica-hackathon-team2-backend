package com.bloom.backend.upload.service;

import com.bloom.backend.global.error.BusinessException;
import com.bloom.backend.global.error.ErrorCode;
import com.bloom.backend.upload.domain.ImagePurpose;
import com.bloom.backend.upload.domain.UploadedImage;
import com.bloom.backend.upload.dto.ImageUploadResponse;
import com.bloom.backend.upload.repository.UploadedImageRepository;
import com.bloom.backend.user.domain.User;
import com.bloom.backend.user.repository.UserRepository;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Arrays;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@Transactional(readOnly = true)
public class ImageUploadService {
    private static final long MAX_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private final UploadedImageRepository imageRepository;
    private final UserRepository userRepository;

    public ImageUploadService(UploadedImageRepository imageRepository, UserRepository userRepository) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ImageUploadResponse upload(Long userId, MultipartFile file, ImagePurpose purpose) {
        if (file == null || file.isEmpty()) throw new BusinessException(ErrorCode.IMAGE_EMPTY);
        if (file.getSize() > MAX_SIZE) throw new BusinessException(ErrorCode.IMAGE_TOO_LARGE);
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.IMAGE_TYPE_UNSUPPORTED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        try {
            byte[] data = file.getBytes();
            if (!matchesSignature(contentType.toLowerCase(), data)) {
                throw new BusinessException(ErrorCode.IMAGE_TYPE_UNSUPPORTED);
            }
            UploadedImage image = imageRepository.save(new UploadedImage(
                    user, purpose, contentType.toLowerCase(), file.getSize(), data,
                    Instant.now().plus(365, ChronoUnit.DAYS)));
            String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/v1/uploads/images/").path(image.getId().toString()).toUriString();
            return new ImageUploadResponse(imageUrl, image.getContentType(), image.getSize());
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public UploadedImage get(Long userId, Long imageId) {
        return imageRepository.findByIdAndUserId(imageId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.IMAGE_NOT_FOUND));
    }

    public String aiImageInput(Long userId, String imageUrl) {
        try {
            String path = URI.create(imageUrl).getPath();
            String prefix = "/api/v1/uploads/images/";
            int start = path.indexOf(prefix);
            if (start < 0) return imageUrl;
            String idValue = path.substring(start + prefix.length());
            UploadedImage image = get(userId, Long.valueOf(idValue));
            return "data:" + image.getContentType() + ";base64," +
                    Base64.getEncoder().encodeToString(image.getData());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_FOUND);
        }
    }

    private boolean matchesSignature(String contentType, byte[] data) {
        return switch (contentType) {
            case "image/jpeg" -> data.length >= 2 && (data[0] & 0xff) == 0xff && (data[1] & 0xff) == 0xd8;
            case "image/png" -> data.length >= 8 && Arrays.equals(Arrays.copyOf(data, 8),
                    new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a});
            case "image/webp" -> data.length >= 12
                    && new String(data, 0, 4, StandardCharsets.US_ASCII).equals("RIFF")
                    && new String(data, 8, 4, StandardCharsets.US_ASCII).equals("WEBP");
            default -> false;
        };
    }
}
