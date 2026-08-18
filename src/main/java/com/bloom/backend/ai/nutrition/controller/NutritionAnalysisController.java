package com.bloom.backend.ai.nutrition.controller;

import com.bloom.backend.ai.nutrition.domain.NutritionInputType;
import com.bloom.backend.ai.nutrition.dto.*;
import com.bloom.backend.ai.nutrition.service.NutritionAnalysisService;
import com.bloom.backend.diary.domain.MealType;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ai/nutrition/analyses")
public class NutritionAnalysisController {
    private final NutritionAnalysisService service;
    public NutritionAnalysisController(NutritionAnalysisService service) { this.service = service; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NutritionAnalysisResponse> analyze(Authentication auth,
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "mealType") MealType mealType,
            @RequestParam(name = "inputType") NutritionInputType inputType,
            @RequestPart(name = "image", required = false) MultipartFile image,
            @RequestParam(name = "text", required = false) String text) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.analyze(userId(auth), date, mealType, inputType, image, text));
    }
    @GetMapping("/{analysisId}")
    public NutritionAnalysisResponse get(Authentication auth, @PathVariable Long analysisId) {
        return service.get(userId(auth), analysisId);
    }
    @PatchMapping("/{analysisId}/foods/{foodId}")
    public DraftFoodResponse patchFood(Authentication auth, @PathVariable Long analysisId, @PathVariable Long foodId,
            @Valid @RequestBody DraftFoodPatchRequest request) {
        return service.patchFood(userId(auth), analysisId, foodId, request);
    }
    @PostMapping("/{analysisId}/foods")
    public ResponseEntity<DraftFoodResponse> addFood(Authentication auth, @PathVariable Long analysisId,
            @Valid @RequestBody DraftFoodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addFood(userId(auth), analysisId, request));
    }
    @DeleteMapping("/{analysisId}/foods/{foodId}")
    public ResponseEntity<Void> deleteFood(Authentication auth, @PathVariable Long analysisId, @PathVariable Long foodId) {
        service.deleteFood(userId(auth), analysisId, foodId); return ResponseEntity.noContent().build();
    }
    @PostMapping("/{analysisId}/record")
    public NutritionRecordResponse record(Authentication auth, @PathVariable Long analysisId) {
        return service.record(userId(auth), analysisId);
    }
    @DeleteMapping("/{analysisId}")
    public ResponseEntity<Void> cancel(Authentication auth, @PathVariable Long analysisId) {
        service.cancel(userId(auth), analysisId); return ResponseEntity.noContent().build();
    }
    private Long userId(Authentication authentication) { return Long.valueOf(authentication.getName()); }
}
