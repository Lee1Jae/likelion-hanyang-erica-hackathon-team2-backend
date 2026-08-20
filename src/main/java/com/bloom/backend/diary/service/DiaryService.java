package com.bloom.backend.diary.service;

import com.bloom.backend.diary.domain.Activity;
import com.bloom.backend.diary.domain.BodyConditionTag;
import com.bloom.backend.diary.domain.Diary;
import com.bloom.backend.diary.domain.EmotionTag;
import com.bloom.backend.diary.domain.Meal;
import com.bloom.backend.diary.domain.SkinTag;
import com.bloom.backend.diary.dto.ActivityRequest;
import com.bloom.backend.diary.dto.ActivityResponse;
import com.bloom.backend.diary.dto.DailyDiaryPatchRequest;
import com.bloom.backend.diary.dto.DailyDiaryResponse;
import com.bloom.backend.diary.dto.DiaryResponse;
import com.bloom.backend.diary.dto.DiaryHistoryItem;
import com.bloom.backend.diary.dto.DiarySaveRequest;
import com.bloom.backend.diary.dto.MealRequest;
import com.bloom.backend.diary.dto.MealResponse;
import com.bloom.backend.diary.repository.ActivityRepository;
import com.bloom.backend.diary.repository.DiaryRepository;
import com.bloom.backend.diary.repository.MealRepository;
import com.bloom.backend.global.error.BusinessException;
import com.bloom.backend.global.error.ErrorCode;
import com.bloom.backend.user.domain.User;
import com.bloom.backend.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DiaryService {
    private static final int MVP_RECOMMENDED_CALORIES = 2000;

    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final MealRepository mealRepository;
    private final ActivityRepository activityRepository;

    public DiaryService(UserRepository userRepository, DiaryRepository diaryRepository,
                        MealRepository mealRepository, ActivityRepository activityRepository) {
        this.userRepository = userRepository;
        this.diaryRepository = diaryRepository;
        this.mealRepository = mealRepository;
        this.activityRepository = activityRepository;
    }

    public DiaryResponse get(Long userId, LocalDate date) {
        return buildDiaryResponse(findDiary(userId, date));
    }

    private DiaryResponse buildDiaryResponse(Diary diary) {
        List<Meal> meals = mealRepository.findAllByDiaryIdOrderByIdAsc(diary.getId());
        List<MealResponse> mealResponses = meals.stream().map(MealResponse::from).toList();
        List<Activity> activities = activityRepository.findAllByDiaryIdOrderByIdAsc(diary.getId());
        int totalCalories = sumNullable(mealResponses.stream().map(MealResponse::kcal).toList());
        boolean nutritionIncomplete = mealResponses.stream().anyMatch(meal -> meal.kcal() == null
                || meal.carbs() == null || meal.protein() == null || meal.fat() == null);
        int totalActivity = activities.stream().mapToInt(Activity::getBurnedKcal).sum();

        var previousDiary = diaryRepository.findByUserIdAndDate(diary.getDiaryUserId(), diary.getDate().minusDays(1));
        Integer calorieChange = previousDiary.map(value -> totalCalories
                - sumNullable(mealRepository.findAllByDiaryIdOrderByIdAsc(value.getId()).stream()
                .map(MealResponse::from).map(MealResponse::kcal).toList())).orElse(null);
        Integer activityChange = previousDiary.map(value -> totalActivity
                - activityRepository.findAllByDiaryIdOrderByIdAsc(value.getId()).stream().mapToInt(Activity::getBurnedKcal).sum()).orElse(null);
        BigDecimal conditionChange = previousDiary
                .filter(value -> diary.getConditionScore() != null && value.getConditionScore() != null)
                .map(value -> diary.getConditionScore().subtract(value.getConditionScore()))
                .orElse(null);

        return new DiaryResponse(diary.getDate(), diary.getMemo(), diary.getConditionScore(), conditionChange,
                diary.getWeightKg(), diary.getWaterMl(), diary.getSkinCondition(), diary.getMenstrualStatus(),
                totalCalories, calorieChange, MVP_RECOMMENDED_CALORIES, MVP_RECOMMENDED_CALORIES - totalCalories,
                totalActivity, activityChange,
                sumNullable(mealResponses.stream().map(MealResponse::carbs).toList()),
                sumNullable(mealResponses.stream().map(MealResponse::protein).toList()),
                sumNullable(mealResponses.stream().map(MealResponse::fat).toList()),
                nutritionIncomplete,
                mealResponses,
                activities.stream().map(ActivityResponse::from).toList());
    }

    public DailyDiaryResponse getDaily(Long userId, LocalDate date) {
        Diary diary = findDiary(userId, date);
        return toDailyResponse(diary, buildDiaryResponse(diary));
    }

    @Transactional
    public DailyDiaryResponse patchDaily(Long userId, DailyDiaryPatchRequest request) {
        if (request.periodStart() != null && request.periodEnd() != null
                && request.periodStart().isAfter(request.periodEnd())) {
            throw new BusinessException(ErrorCode.DATE_RANGE_INVALID);
        }
        Diary diary = getOrCreateDiary(userId, request.date());
        String skinConditions = join(request.skin());
        diary.patchDaily(request.weightKg(), request.emotionScore(), request.bodyScore(),
                join(request.emotionTags()), join(request.bodyTags()), request.waterMl(), skinConditions,
                request.periodStart(), request.periodEnd(), request.memo());
        return toDailyResponse(diary, buildDiaryResponse(diary));
    }

    public List<DiaryHistoryItem> getHistory(Long userId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException(ErrorCode.DATE_RANGE_INVALID);
        }
        List<Diary> diaries = diaryRepository.findAllByUserIdAndDateBetweenOrderByDateAsc(userId, from, to);
        if (diaries.isEmpty()) {
            return List.of();
        }
        List<Long> diaryIds = diaries.stream().map(Diary::getId).toList();
        Map<Long, List<Meal>> mealsByDiaryId = mealRepository
                .findAllByDiaryIdInOrderByDiaryIdAscIdAsc(diaryIds).stream()
                .collect(Collectors.groupingBy(meal -> meal.getDiary().getId()));
        Map<Long, List<Activity>> activitiesByDiaryId = activityRepository
                .findAllByDiaryIdInOrderByDiaryIdAscIdAsc(diaryIds).stream()
                .collect(Collectors.groupingBy(activity -> activity.getDiary().getId()));
        return diaries.stream().map(diary -> {
            List<Meal> meals = mealsByDiaryId.getOrDefault(diary.getId(), Collections.emptyList());
            List<MealResponse> mealResponses = meals.stream().map(MealResponse::from).toList();
            List<Activity> activities = activitiesByDiaryId.getOrDefault(diary.getId(), Collections.emptyList());
            return new DiaryHistoryItem(diary.getDate(), diary.getWeightKg(), diary.getEmotionScore(),
                    diary.getBodyScore(), diary.getWaterMl(),
                    sumNullable(mealResponses.stream().map(MealResponse::kcal).toList()),
                    activities.stream().mapToInt(Activity::getSteps).sum(),
                    activities.stream().mapToInt(Activity::getExerciseMinutes).sum(),
                    activities.stream().mapToInt(Activity::getBurnedKcal).sum(),
                    splitEnums(diary.getEmotionTags(), EmotionTag.class),
                    splitEnums(diary.getBodyTags(), BodyConditionTag.class),
                    splitEnums(diary.getSkinConditions(), SkinTag.class),
                    diary.getPeriodStart(), diary.getPeriodEnd());
        }).toList();
    }

    @Transactional
    public DiaryResponse save(Long userId, LocalDate date, DiarySaveRequest request) {
        Diary diary = diaryRepository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> new Diary(findUser(userId), date));
        diary.update(request.memo(), request.condition(), request.weight(), request.waterIntake(),
                request.skinCondition(), request.menstrualStatus());
        diaryRepository.save(diary);
        return buildDiaryResponse(diary);
    }

    @Transactional
    public MealResponse createMeal(Long userId, LocalDate date, MealRequest request) {
        Diary diary = getOrCreateDiary(userId, date);
        return MealResponse.from(mealRepository.save(new Meal(diary, request.mealType(), request.foodName(),
                request.kcal(), request.carbs(), request.protein(), request.fat())));
    }

    @Transactional
    public MealResponse createMealFromNutritionAnalysis(Long userId, LocalDate date, MealRequest request,
                                                         Long analysisId, String sourceImageUrl) {
        Diary diary = getOrCreateDiary(userId, date);
        return MealResponse.from(mealRepository.save(new Meal(diary, request.mealType(), request.foodName(),
                request.kcal(), request.carbs(), request.protein(), request.fat(), analysisId, sourceImageUrl)));
    }

    @Transactional
    public MealResponse updateMeal(Long userId, Long mealId, MealRequest request) {
        Meal meal = mealRepository.findByIdAndDiaryUserId(mealId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEAL_NOT_FOUND));
        meal.update(request.mealType(), request.foodName(), request.kcal(), request.carbs(), request.protein(), request.fat());
        return MealResponse.from(meal);
    }

    @Transactional
    public void deleteMeal(Long userId, Long mealId) {
        Meal meal = mealRepository.findByIdAndDiaryUserId(mealId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEAL_NOT_FOUND));
        mealRepository.delete(meal);
    }

    @Transactional
    public ActivityResponse createActivity(Long userId, LocalDate date, ActivityRequest request) {
        Diary diary = getOrCreateDiary(userId, date);
        return ActivityResponse.from(activityRepository.save(new Activity(diary, request.steps(),
                request.exerciseMinutes(), request.burnedKcal(), request.memo())));
    }

    @Transactional
    public ActivityResponse updateActivity(Long userId, Long activityId, ActivityRequest request) {
        Activity activity = activityRepository.findByIdAndDiaryUserId(activityId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));
        activity.update(request.steps(), request.exerciseMinutes(), request.burnedKcal(), request.memo());
        return ActivityResponse.from(activity);
    }

    @Transactional
    public void deleteActivity(Long userId, Long activityId) {
        Activity activity = activityRepository.findByIdAndDiaryUserId(activityId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));
        activityRepository.delete(activity);
    }

    private Diary getOrCreateDiary(Long userId, LocalDate date) {
        return diaryRepository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> diaryRepository.save(new Diary(findUser(userId), date)));
    }

    private Diary findDiary(Long userId, LocalDate date) {
        return diaryRepository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private DailyDiaryResponse toDailyResponse(Diary diary, DiaryResponse legacy) {
        var previousDiary = diaryRepository.findByUserIdAndDate(
                diary.getDiaryUserId(), diary.getDate().minusDays(1));
        List<Activity> previousActivities = previousDiary
                .map(value -> activityRepository.findAllByDiaryIdOrderByIdAsc(value.getId())).orElse(List.of());
        int totalSteps = legacy.activities().stream().mapToInt(ActivityResponse::steps).sum();
        int totalExerciseMinutes = legacy.activities().stream().mapToInt(ActivityResponse::exerciseMinutes).sum();
        int totalBurnedKcal = legacy.activities().stream().mapToInt(ActivityResponse::burnedKcal).sum();
        Integer stepsChange = previousDiary.isEmpty() ? null
                : totalSteps - previousActivities.stream().mapToInt(Activity::getSteps).sum();
        Integer exerciseMinutesChange = previousDiary.isEmpty() ? null
                : totalExerciseMinutes - previousActivities.stream().mapToInt(Activity::getExerciseMinutes).sum();
        Integer burnedKcalChange = previousDiary.isEmpty() ? null
                : totalBurnedKcal - previousActivities.stream().mapToInt(Activity::getBurnedKcal).sum();
        return new DailyDiaryResponse(diary.getDate(), diary.getWeightKg(), diary.getEmotionScore(),
                diary.getBodyScore(), splitEnums(diary.getEmotionTags(), EmotionTag.class),
                splitEnums(diary.getBodyTags(), BodyConditionTag.class),
                diary.getWaterMl(), splitEnums(diary.getSkinConditions(), SkinTag.class),
                diary.getPeriodStart(), diary.getPeriodEnd(), diary.getMemo(),
                legacy.totalCalories(), legacy.calorieChange(), legacy.recommendedCalories(),
                legacy.remainingCalories(), legacy.nutritionIncomplete(), totalSteps, stepsChange, totalExerciseMinutes,
                exerciseMinutesChange, totalBurnedKcal, burnedKcalChange,
                legacy.meals(), legacy.activities());
    }

    private String join(List<?> values) {
        return values == null ? null : values.stream().map(Object::toString)
                .reduce((left, right) -> left + "," + right).orElse("");
    }

    private List<String> split(String values) {
        return values == null || values.isBlank() ? List.of() : List.of(values.split(","));
    }

    private <E extends Enum<E>> List<E> splitEnums(String values, Class<E> enumType) {
        return split(values).stream().map(value -> Enum.valueOf(enumType, value)).toList();
    }

    private int sumNullable(List<Integer> values) {
        return values.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
    }
}
