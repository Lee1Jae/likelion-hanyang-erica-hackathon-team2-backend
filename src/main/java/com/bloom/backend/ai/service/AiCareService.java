package com.bloom.backend.ai.service;

import com.bloom.backend.ai.client.OpenAiResponsesClient;
import com.bloom.backend.ai.domain.AiReport;
import com.bloom.backend.ai.dto.*;
import com.bloom.backend.ai.repository.AiReportRepository;
import com.bloom.backend.care.domain.BodyCheck;
import com.bloom.backend.care.repository.BodyCheckRepository;
import com.bloom.backend.global.error.*;
import com.bloom.backend.upload.service.ImageUploadService;
import com.bloom.backend.user.domain.User;
import com.bloom.backend.user.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class AiCareService {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final String PROCEDURE_INSTRUCTIONS = """
            산후 바디케어 정보 제공용 추천 엔진이다. 의료 진단이나 치료 보장을 하지 않는다.
            제공된 목표·건강 이슈·집중 부위·눈바디 기록 메타데이터만 사용한다.
            근거가 없는 가격, 횟수, 간격은 반드시 null로 반환한다. 최대 3개를 한국어로 추천하고,
            위험하거나 금기 가능성이 있으면 전문 의료진 상담 필요성을 reason에 명시한다.
            """;
    private static final String REPORT_INSTRUCTIONS = """
            산후 건강·생활 기록 리포트를 생성한다. 의료 진단을 하지 않고 입력 JSON에 없는 사실은 추측하지 않는다.
            null과 미기록을 나쁜 상태나 0으로 해석하지 않는다. 날짜별 합계와 변화 추세를 근거로 짧은 종합 요약,
            가장 중요한 관리 우선순위 최대 3개, 실행 가능한 방법 최대 3개를 한국어로 반환한다.
            """;

    private final BodyCheckRepository bodyCheckRepository;
    private final AiReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AiContextService contextService;
    private final OpenAiResponsesClient aiClient;
    private final ObjectMapper objectMapper;
    private final ImageUploadService imageUploadService;

    public AiCareService(BodyCheckRepository bodyCheckRepository, AiReportRepository reportRepository,
                         UserRepository userRepository, AiContextService contextService,
                         OpenAiResponsesClient aiClient, ObjectMapper objectMapper,
                         ImageUploadService imageUploadService) {
        this.bodyCheckRepository = bodyCheckRepository; this.reportRepository = reportRepository;
        this.userRepository = userRepository; this.contextService = contextService;
        this.aiClient = aiClient; this.objectMapper = objectMapper;
        this.imageUploadService = imageUploadService;
    }

    public ProcedureRecommendationsResponse recommendProcedures(Long userId, ProcedureRecommendationRequest request) {
        BodyCheck bodyCheck = bodyCheckRepository.findByIdAndUserId(request.bodyCheckId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BODY_CHECK_NOT_FOUND));
        LocalDate today = LocalDate.now(SEOUL);
        String profile = contextService.context(userId, today.minusDays(29), today);
        Map<String, Object> personalization = Map.of("bodyCheck", Map.of(
                "bodyCheckId", bodyCheck.getId(), "recordedDate", bodyCheck.getRecordedDate()),
                "personalization", profile);
        String imageInput = imageUploadService.aiImageInput(userId, bodyCheck.getOriginalImageUrl());
        Object input = List.of(Map.of("role", "user", "content", List.of(
                Map.of("type", "input_text", "text", "추천 입력(JSON): " + objectMapper.valueToTree(personalization)),
                Map.of("type", "input_image", "detail", "low", "image_url", imageInput))));
        JsonNode result = aiClient.structured(PROCEDURE_INSTRUCTIONS, input,
                "procedure_recommendations", procedureSchema());
        List<ProcedureRecommendationItem> recommendations = objectMapper.convertValue(
                result.path("recommendations"), new TypeReference<>() {});
        return new ProcedureRecommendationsResponse(recommendations, Instant.now());
    }

    public AiReportResponse createReport(Long userId, AiReportRequest request) {
        if (request.from().isAfter(request.to()) || request.from().plusDays(30).isBefore(request.to())) {
            throw new BusinessException(ErrorCode.DATE_RANGE_INVALID);
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        AiReport report = reportRepository.save(new AiReport(user, request.from(), request.to()));
        try {
            String context = contextService.context(userId, request.from(), request.to());
            JsonNode result = aiClient.structured(REPORT_INSTRUCTIONS, context, "health_report", reportSchema());
            String summary = result.path("summary").asText();
            String priorities = objectMapper.writeValueAsString(result.path("priorities"));
            String methods = objectMapper.writeValueAsString(result.path("methods"));
            report.complete(summary, priorities, methods, aiClient.model());
            return response(reportRepository.save(report));
        } catch (BusinessException exception) {
            report.fail(); reportRepository.save(report); throw exception;
        } catch (Exception exception) {
            report.fail(); reportRepository.save(report); throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    public AiReportResponse getReport(Long userId, Long reportId) {
        return response(reportRepository.findByIdAndUserId(reportId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_REPORT_NOT_FOUND)));
    }

    public AiReportResponse latestReport(Long userId) {
        return response(reportRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_REPORT_NOT_FOUND)));
    }

    private AiReportResponse response(AiReport report) {
        try {
            List<AiReportItem> priorities = report.getPrioritiesJson() == null ? List.of()
                    : objectMapper.readValue(report.getPrioritiesJson(), new TypeReference<>() {});
            List<AiReportItem> methods = report.getMethodsJson() == null ? List.of()
                    : objectMapper.readValue(report.getMethodsJson(), new TypeReference<>() {});
            Instant generatedAt = report.getUpdatedAt() == null ? report.getCreatedAt() : report.getUpdatedAt();
            return new AiReportResponse(report.getId(), report.getFromDate(), report.getToDate(), report.getStatus(),
                    report.getSummary(), priorities, methods, generatedAt);
        } catch (Exception exception) { throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR); }
    }

    private JsonNode procedureSchema() {
        Map<String, Object> nullableString = Map.of("type", List.of("string", "null"));
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("type", "object");
        item.put("properties", Map.of(
                "procedureId", Map.of("type", "string"), "name", Map.of("type", "string"),
                "description", Map.of("type", "string"), "reason", Map.of("type", "string"),
                "estimatedSessions", nullableString, "interval", nullableString,
                "estimatedPrice", Map.of("type", List.of("integer", "null"))));
        item.put("required", List.of("procedureId", "name", "description", "reason",
                "estimatedSessions", "interval", "estimatedPrice"));
        item.put("additionalProperties", false);
        return objectMapper.valueToTree(Map.of("type", "object", "properties", Map.of(
                "recommendations", Map.of("type", "array", "maxItems", 3, "items", item)),
                "required", List.of("recommendations"), "additionalProperties", false));
    }

    private JsonNode reportSchema() {
        Map<String, Object> item = Map.of("type", "object", "properties", Map.of(
                "title", Map.of("type", "string"), "description", Map.of("type", "string")),
                "required", List.of("title", "description"), "additionalProperties", false);
        return objectMapper.valueToTree(Map.of("type", "object", "properties", Map.of(
                "summary", Map.of("type", "string"),
                "priorities", Map.of("type", "array", "maxItems", 3, "items", item),
                "methods", Map.of("type", "array", "maxItems", 3, "items", item)),
                "required", List.of("summary", "priorities", "methods"), "additionalProperties", false));
    }
}
