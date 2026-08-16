package com.bloom.backend.diary.controller;

import com.bloom.backend.diary.dto.ActivityRequest;
import com.bloom.backend.diary.dto.ActivityResponse;
import com.bloom.backend.diary.dto.DailyDiaryPatchRequest;
import com.bloom.backend.diary.dto.DailyDiaryResponse;
import com.bloom.backend.diary.dto.DiaryHistoryItem;
import com.bloom.backend.diary.dto.DiaryResponse;
import com.bloom.backend.diary.dto.DiarySaveRequest;
import com.bloom.backend.diary.dto.MealRequest;
import com.bloom.backend.diary.dto.MealResponse;
import com.bloom.backend.diary.service.DiaryService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DiaryController {
    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @GetMapping("/diary/daily")
    public DailyDiaryResponse getDaily(Authentication auth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date == null ? LocalDate.now(ZoneId.of("Asia/Seoul")) : date;
        return diaryService.getDaily(userId(auth), targetDate);
    }

    @PatchMapping("/diary/daily")
    public DailyDiaryResponse patchDaily(Authentication auth, @Valid @RequestBody DailyDiaryPatchRequest request) {
        return diaryService.patchDaily(userId(auth), request);
    }

    @GetMapping("/diary/history")
    public List<DiaryHistoryItem> getHistory(Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return diaryService.getHistory(userId(auth), from, to);
    }

    @GetMapping("/diaries/{date}")
    public DiaryResponse get(Authentication auth, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return diaryService.get(userId(auth), date);
    }

    @PutMapping("/diaries/{date}")
    public DiaryResponse save(Authentication auth, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              @Valid @RequestBody DiarySaveRequest request) {
        return diaryService.save(userId(auth), date, request);
    }

    @PostMapping("/diaries/{date}/meals")
    public ResponseEntity<MealResponse> createMeal(Authentication auth,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody MealRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryService.createMeal(userId(auth), date, request));
    }

    @PatchMapping("/meals/{mealId}")
    public MealResponse updateMeal(Authentication auth, @PathVariable Long mealId, @Valid @RequestBody MealRequest request) {
        return diaryService.updateMeal(userId(auth), mealId, request);
    }

    @DeleteMapping("/meals/{mealId}")
    public ResponseEntity<Void> deleteMeal(Authentication auth, @PathVariable Long mealId) {
        diaryService.deleteMeal(userId(auth), mealId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/diaries/{date}/activities")
    public ResponseEntity<ActivityResponse> createActivity(Authentication auth,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryService.createActivity(userId(auth), date, request));
    }

    @PatchMapping("/activities/{activityId}")
    public ActivityResponse updateActivity(Authentication auth, @PathVariable Long activityId,
                                           @Valid @RequestBody ActivityRequest request) {
        return diaryService.updateActivity(userId(auth), activityId, request);
    }

    @DeleteMapping("/activities/{activityId}")
    public ResponseEntity<Void> deleteActivity(Authentication auth, @PathVariable Long activityId) {
        diaryService.deleteActivity(userId(auth), activityId);
        return ResponseEntity.noContent().build();
    }

    private Long userId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }
}
