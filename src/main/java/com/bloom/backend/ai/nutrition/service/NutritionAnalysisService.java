package com.bloom.backend.ai.nutrition.service;

import com.bloom.backend.ai.client.OpenAiResponsesClient;
import com.bloom.backend.ai.nutrition.domain.*;
import com.bloom.backend.ai.nutrition.dto.*;
import com.bloom.backend.ai.nutrition.repository.*;
import com.bloom.backend.diary.domain.MealType;
import com.bloom.backend.diary.dto.*;
import com.bloom.backend.diary.service.DiaryService;
import com.bloom.backend.global.error.*;
import com.bloom.backend.upload.domain.ImagePurpose;
import com.bloom.backend.upload.dto.ImageUploadResponse;
import com.bloom.backend.upload.service.ImageUploadService;
import com.bloom.backend.user.domain.User;
import com.bloom.backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class NutritionAnalysisService {
    private static final String INSTRUCTIONS = """
            음식 사진 또는 설명에서 서로 다른 음식을 최대 10개 식별한다. 음식의 양과 kcal·탄수화물·단백질·지방을
            현실적인 범위에서 추정하되 확인할 수 없으면 반드시 null로 반환한다. 사진에 없는 음식은 만들지 않는다.
            탄수화물·단백질·지방을 모두 추정했다면 kcal은 대략 탄수화물×4 + 단백질×4 + 지방×9와 일관되어야 한다.
            영양정보를 모르는 경우 0으로 채우지 말고 null로 반환한다.
            영양값은 모두 1회 제공량 기준의 0 이상 정수이고, confidence는 0~1이다. 의료 조언은 하지 않는다.
            """;
    private final NutritionAnalysisRepository analysisRepository;
    private final DraftFoodRepository foodRepository;
    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;
    private final OpenAiResponsesClient aiClient;
    private final DiaryService diaryService;
    private final ObjectMapper objectMapper;

    public NutritionAnalysisService(NutritionAnalysisRepository analysisRepository, DraftFoodRepository foodRepository,
            UserRepository userRepository, ImageUploadService imageUploadService, OpenAiResponsesClient aiClient,
            DiaryService diaryService, ObjectMapper objectMapper) {
        this.analysisRepository = analysisRepository; this.foodRepository = foodRepository;
        this.userRepository = userRepository; this.imageUploadService = imageUploadService; this.aiClient = aiClient;
        this.diaryService = diaryService; this.objectMapper = objectMapper;
    }

    public NutritionAnalysisResponse analyze(Long userId, LocalDate date, MealType mealType,
            NutritionInputType inputType, MultipartFile image, String text) {
        validateInput(inputType, image, text);
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String imageUrl = null;
        Object input;
        try {
            if (inputType == NutritionInputType.IMAGE) {
                ImageUploadResponse uploaded = imageUploadService.upload(userId, image, ImagePurpose.NUTRITION);
                imageUrl = uploaded.imageUrl();
                String dataUrl = "data:" + image.getContentType() + ";base64," +
                        Base64.getEncoder().encodeToString(image.getBytes());
                input = List.of(Map.of("role", "user", "content", List.of(
                        Map.of("type", "input_text", "text", "사진 속 모든 음식과 양, 영양성분을 분석해 주세요."),
                        Map.of("type", "input_image", "detail", "auto", "image_url", dataUrl))));
            } else {
                input = text.trim();
            }
        } catch (Exception exception) {
            if (exception instanceof BusinessException businessException) throw businessException;
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }
        NutritionAnalysis analysis = analysisRepository.save(new NutritionAnalysis(
                user, date, mealType, inputType, imageUrl, inputType == NutritionInputType.TEXT ? text.trim() : null,
                aiClient.model()));
        try {
            JsonNode result = aiClient.structured(INSTRUCTIONS, input, "nutrition_analysis", nutritionSchema());
            for (JsonNode food : result.path("foods")) {
                foodRepository.save(new DraftFood(analysis, food.path("foodName").asText(), decimal(food, "amount"),
                        nullableText(food, "amountUnit"), integer(food, "kcal"), integer(food, "carbs"),
                        integer(food, "protein"), integer(food, "fat"), decimal(food, "confidence"),
                        NutritionSource.AI_ESTIMATE));
            }
            if (foodRepository.findAllByAnalysisIdOrderByIdAsc(analysis.getId()).isEmpty()) {
                analysis.fail(); analysisRepository.save(analysis);
            }
            return response(analysis);
        } catch (BusinessException exception) {
            analysis.fail(); analysisRepository.save(analysis); throw exception;
        }
    }

    public NutritionAnalysisResponse get(Long userId, Long analysisId) { return response(find(userId, analysisId)); }

    @Transactional
    public DraftFoodResponse patchFood(Long userId, Long analysisId, Long foodId, DraftFoodPatchRequest request) {
        NutritionAnalysis analysis = editable(userId, analysisId);
        if (!request.hasChanges()) throw new BusinessException(ErrorCode.COMMON_INVALID_INPUT);
        if (request.foodNamePresent() && request.foodName() == null) {
            throw new BusinessException(ErrorCode.COMMON_INVALID_INPUT);
        }
        DraftFood food = foodRepository.findByIdAndAnalysisId(foodId, analysis.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NUTRITION_DRAFT_FOOD_NOT_FOUND));
        if (request.foodNamePresent()) food.patchFoodName(request.foodName());
        if (request.amountPresent()) food.patchAmount(request.amount());
        if (request.amountUnitPresent()) food.patchAmountUnit(request.amountUnit());
        if (request.kcalPresent()) food.patchKcal(request.kcal());
        if (request.carbsPresent()) food.patchCarbs(request.carbs());
        if (request.proteinPresent()) food.patchProtein(request.protein());
        if (request.fatPresent()) food.patchFat(request.fat());
        food.normalizeNutrition();
        return DraftFoodResponse.from(foodRepository.save(food));
    }

    @Transactional
    public DraftFoodResponse addFood(Long userId, Long analysisId, DraftFoodRequest request) {
        NutritionAnalysis analysis = find(userId, analysisId);
        if (analysis.getStatus() != NutritionAnalysisStatus.DRAFT
                && analysis.getStatus() != NutritionAnalysisStatus.FAILED) stateError();
        analysis.draft(); analysisRepository.save(analysis);
        return DraftFoodResponse.from(foodRepository.save(new DraftFood(analysis, request.foodName(), request.amount(),
                request.amountUnit(), request.kcal(), request.carbs(), request.protein(), request.fat(),
                null, NutritionSource.USER_INPUT)));
    }

    @Transactional
    public void deleteFood(Long userId, Long analysisId, Long foodId) {
        NutritionAnalysis analysis = editable(userId, analysisId);
        DraftFood food = foodRepository.findByIdAndAnalysisId(foodId, analysis.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NUTRITION_DRAFT_FOOD_NOT_FOUND));
        foodRepository.delete(food);
    }

    @Transactional
    public NutritionRecordResponse record(Long userId, Long analysisId) {
        NutritionAnalysis analysis = editable(userId, analysisId);
        List<DraftFood> foods = foodRepository.findAllByAnalysisIdOrderByIdAsc(analysisId);
        if (foods.isEmpty()) throw new BusinessException(ErrorCode.NUTRITION_ANALYSIS_STATE_INVALID);
        List<MealResponse> meals = foods.stream().map(food -> diaryService.createMealFromNutritionAnalysis(
                userId, analysis.getDate(),
                new MealRequest(analysis.getMealType(), food.getFoodName(), food.getKcal(), food.getCarbs(),
                        food.getProtein(), food.getFat()), analysis.getId(), analysis.getImageUrl())).toList();
        analysis.record(); analysisRepository.save(analysis);
        return new NutritionRecordResponse(analysis.getId(), analysis.getStatus(), meals);
    }

    @Transactional
    public void cancel(Long userId, Long analysisId) {
        NutritionAnalysis analysis = find(userId, analysisId);
        if (analysis.getStatus() == NutritionAnalysisStatus.RECORDED) stateError();
        foodRepository.deleteAllByAnalysisId(analysisId); analysisRepository.delete(analysis);
    }

    private NutritionAnalysisResponse response(NutritionAnalysis analysis) {
        List<DraftFoodResponse> foods = foodRepository.findAllByAnalysisIdOrderByIdAsc(analysis.getId()).stream()
                .map(DraftFoodResponse::from).toList();
        List<Integer> known = foods.stream().map(DraftFoodResponse::kcal).filter(Objects::nonNull).toList();
        Integer totalKcal = known.isEmpty() ? null : known.stream().mapToInt(Integer::intValue).sum();
        return new NutritionAnalysisResponse(analysis.getId(), analysis.getStatus(), analysis.getModelVersion(),
                analysis.getImageUrl(), foods, totalKcal, analysis.getStatus() == NutritionAnalysisStatus.FAILED);
    }

    private NutritionAnalysis find(Long userId, Long id) {
        return analysisRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NUTRITION_ANALYSIS_NOT_FOUND));
    }
    private NutritionAnalysis editable(Long userId, Long id) {
        NutritionAnalysis analysis = find(userId, id);
        if (analysis.getStatus() != NutritionAnalysisStatus.DRAFT) stateError();
        return analysis;
    }
    private void stateError() { throw new BusinessException(ErrorCode.NUTRITION_ANALYSIS_STATE_INVALID); }
    private void validateInput(NutritionInputType type, MultipartFile image, String text) {
        boolean hasImage = image != null && !image.isEmpty(); boolean hasText = text != null && !text.isBlank();
        if ((type == NutritionInputType.IMAGE && (!hasImage || hasText))
                || (type == NutritionInputType.TEXT && (!hasText || hasImage))) {
            throw new BusinessException(ErrorCode.NUTRITION_INPUT_INVALID);
        }
    }
    private Integer integer(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.intValue();
    }
    private BigDecimal decimal(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.decimalValue();
    }
    private String nullableText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private JsonNode nutritionSchema() {
        Map<String, Object> nullableNumber = Map.of("type", List.of("number", "null"));
        Map<String, Object> nullableInteger = Map.of("type", List.of("integer", "null"), "minimum", 0);
        Map<String, Object> food = new LinkedHashMap<>();
        food.put("type", "object");
        food.put("properties", Map.of("foodName", Map.of("type", "string"), "amount", nullableNumber,
                "amountUnit", Map.of("type", List.of("string", "null")), "kcal", nullableInteger,
                "carbs", nullableInteger, "protein", nullableInteger, "fat", nullableInteger,
                "confidence", Map.of("type", List.of("number", "null"), "minimum", 0, "maximum", 1)));
        food.put("required", List.of("foodName", "amount", "amountUnit", "kcal", "carbs", "protein", "fat", "confidence"));
        food.put("additionalProperties", false);
        return objectMapper.valueToTree(Map.of("type", "object", "properties",
                Map.of("foods", Map.of("type", "array", "maxItems", 10, "items", food)),
                "required", List.of("foods"), "additionalProperties", false));
    }
}
