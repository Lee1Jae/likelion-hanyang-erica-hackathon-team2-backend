package com.bloom.backend.ai.controller;

import com.bloom.backend.ai.dto.*;
import com.bloom.backend.ai.service.AiCareService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiCareController {
    private final AiCareService aiCareService;
    public AiCareController(AiCareService aiCareService) { this.aiCareService = aiCareService; }
    @PostMapping("/procedures/recommendations")
    public ProcedureRecommendationsResponse procedures(Authentication auth,
            @Valid @RequestBody ProcedureRecommendationRequest request) {
        return aiCareService.recommendProcedures(userId(auth), request);
    }
    @PostMapping("/reports")
    public AiReportResponse createReport(Authentication auth, @Valid @RequestBody AiReportRequest request) {
        return aiCareService.createReport(userId(auth), request);
    }
    @GetMapping("/reports/{reportId}")
    public AiReportResponse report(Authentication auth, @PathVariable Long reportId) {
        return aiCareService.getReport(userId(auth), reportId);
    }
    @GetMapping("/reports/latest")
    public AiReportResponse latest(Authentication auth) { return aiCareService.latestReport(userId(auth)); }
    private Long userId(Authentication authentication) { return Long.valueOf(authentication.getName()); }
}
