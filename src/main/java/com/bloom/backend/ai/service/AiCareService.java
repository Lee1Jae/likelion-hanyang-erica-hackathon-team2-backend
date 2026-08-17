package com.bloom.backend.ai.service;

import com.bloom.backend.ai.dto.*;
import com.bloom.backend.care.repository.BodyCheckRepository;
import com.bloom.backend.global.error.*;
import org.springframework.stereotype.Service;

@Service
public class AiCareService {
    private final BodyCheckRepository bodyCheckRepository;
    public AiCareService(BodyCheckRepository bodyCheckRepository) { this.bodyCheckRepository = bodyCheckRepository; }

    public ProcedureRecommendationsResponse recommendProcedures(Long userId, ProcedureRecommendationRequest request) {
        bodyCheckRepository.findByIdAndUserId(request.bodyCheckId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BODY_CHECK_NOT_FOUND));
        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    public AiReportResponse createReport(Long userId, AiReportRequest request) {
        if (request.from().isAfter(request.to())) throw new BusinessException(ErrorCode.DATE_RANGE_INVALID);
        throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    public AiReportResponse getReport(Long userId, Long reportId) {
        throw new BusinessException(ErrorCode.AI_REPORT_NOT_FOUND);
    }

    public AiReportResponse latestReport(Long userId) {
        throw new BusinessException(ErrorCode.AI_REPORT_NOT_FOUND);
    }
}
