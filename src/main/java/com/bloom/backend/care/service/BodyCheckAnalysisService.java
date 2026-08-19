package com.bloom.backend.care.service;

import com.bloom.backend.ai.client.OpenAiResponsesClient;
import com.bloom.backend.care.dto.BodyCheckResponse;
import com.bloom.backend.global.error.BusinessException;
import com.bloom.backend.global.error.ErrorCode;
import com.bloom.backend.upload.domain.ImagePurpose;
import com.bloom.backend.upload.dto.ImageUploadResponse;
import com.bloom.backend.upload.service.ImageUploadService;
import org.springframework.stereotype.Service;

@Service
public class BodyCheckAnalysisService {
    private static final String PROMPT = """
            Edit the supplied photo into a conservative, realistic wellness progress visualization.
            Preserve the same adult person's identity, face, pose, clothing coverage, background, and camera angle.
            Show only subtle, plausible changes associated with healthy posture and general fitness.
            Do not depict surgery, a medical outcome, extreme weight loss, pregnancy changes, nudity, or sexualized content.
            Do not add text, measurements, labels, or before/after graphics. This is an illustrative estimate, not a guarantee.
            """;

    private final BodyCheckService bodyCheckService;
    private final ImageUploadService imageUploadService;
    private final OpenAiResponsesClient aiClient;

    public BodyCheckAnalysisService(BodyCheckService bodyCheckService,
                                    ImageUploadService imageUploadService,
                                    OpenAiResponsesClient aiClient) {
        this.bodyCheckService = bodyCheckService;
        this.imageUploadService = imageUploadService;
        this.aiClient = aiClient;
    }

    public BodyCheckResponse analyze(Long userId, Long bodyCheckId) {
        BodyCheckService.AnalysisTarget target = bodyCheckService.startAnalysis(userId, bodyCheckId);
        try {
            String imageInput = imageUploadService.aiImageInput(userId, target.originalImageUrl());
            byte[] expectedImage = aiClient.editImage(PROMPT, imageInput);
            ImageUploadResponse stored = imageUploadService.storeGenerated(
                    userId, expectedImage, "image/png", ImagePurpose.BODY_CHECK);
            return bodyCheckService.completeAnalysis(userId, target.bodyCheckId(), stored.imageUrl());
        } catch (BusinessException exception) {
            bodyCheckService.failAnalysis(userId, target.bodyCheckId());
            if (exception.getErrorCode() == ErrorCode.BODY_CHECK_NOT_FOUND
                    || exception.getErrorCode() == ErrorCode.IMAGE_NOT_FOUND) {
                throw exception;
            }
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        } catch (RuntimeException exception) {
            bodyCheckService.failAnalysis(userId, target.bodyCheckId());
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }
    }
}
