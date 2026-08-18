package com.bloom.backend.ai.service;

import com.bloom.backend.diary.service.DiaryService;
import com.bloom.backend.period.repository.PeriodRecordRepository;
import com.bloom.backend.user.domain.UserProfile;
import com.bloom.backend.user.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class AiContextService {
    private final UserProfileRepository profileRepository;
    private final PeriodRecordRepository periodRepository;
    private final DiaryService diaryService;
    private final ObjectMapper objectMapper;

    public AiContextService(UserProfileRepository profileRepository, PeriodRecordRepository periodRepository,
                            DiaryService diaryService, ObjectMapper objectMapper) {
        this.profileRepository = profileRepository; this.periodRepository = periodRepository;
        this.diaryService = diaryService; this.objectMapper = objectMapper;
    }

    public String context(Long userId, LocalDate from, LocalDate to) {
        Map<String, Object> root = new LinkedHashMap<>();
        profileRepository.findByUserId(userId).ifPresent(profile -> root.put("profile", profile(profile)));
        root.put("dailyRecords", diaryService.getHistory(userId, from, to));
        root.put("periods", periodRepository.findAllByUserIdOrderByStartDateDescIdDesc(userId).stream()
                .filter(record -> !record.getEndDate().isBefore(from) && !record.getStartDate().isAfter(to))
                .map(record -> Map.of("startDate", record.getStartDate(), "endDate", record.getEndDate())).toList());
        try { return objectMapper.writeValueAsString(root); }
        catch (Exception exception) { return "{}"; }
    }

    private Map<String, Object> profile(UserProfile profile) {
        Map<String, Object> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        if (profile.getBirthDate() != null) result.put("age", ChronoUnit.YEARS.between(profile.getBirthDate(), today));
        if (profile.getDeliveryDate() != null) result.put("postpartumDays", ChronoUnit.DAYS.between(profile.getDeliveryDate(), today));
        result.put("heightCm", profile.getHeightCm()); result.put("weightKg", profile.getWeightKg());
        result.put("beautyGoals", split(profile.getBeautyGoals())); result.put("healthIssues", split(profile.getHealthIssues()));
        result.put("focusAreas", split(profile.getFocusAreas())); result.put("recoveryAreas", split(profile.getRecoveryAreas()));
        result.put("skinConcerns", split(profile.getSkinConcerns())); result.put("cycleLength", profile.getCycleLength());
        return result;
    }

    private List<String> split(String value) {
        return value == null || value.isBlank() ? List.of() : Arrays.asList(value.split(","));
    }
}
