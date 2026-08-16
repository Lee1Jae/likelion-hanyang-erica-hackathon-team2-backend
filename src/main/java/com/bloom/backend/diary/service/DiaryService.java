package com.bloom.backend.diary.service;

import com.bloom.backend.diary.domain.Activity;
import com.bloom.backend.diary.domain.Diary;
import com.bloom.backend.diary.domain.Meal;
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
import java.util.List;
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
        Diary diary = findDiary(userId, date);
        List<Meal> meals = mealRepository.findAllByDiaryIdOrderByIdAsc(diary.getId());
        List<Activity> activities = activityRepository.findAllByDiaryIdOrderByIdAsc(diary.getId());
        int totalCalories = meals.stream().mapToInt(Meal::getCalories).sum();
        int totalActivity = activities.stream().mapToInt(Activity::getActivityAmount).sum();

        LocalDate previousDate = date.minusDays(1);
        var previousDiary = diaryRepository.findByUserIdAndDate(userId, previousDate);
        Integer calorieChange = previousDiary.map(value -> totalCalories
                - mealRepository.findAllByDiaryIdOrderByIdAsc(value.getId()).stream().mapToInt(Meal::getCalories).sum()).orElse(null);
        Integer activityChange = previousDiary.map(value -> totalActivity
                - activityRepository.findAllByDiaryIdOrderByIdAsc(value.getId()).stream().mapToInt(Activity::getActivityAmount).sum()).orElse(null);
        BigDecimal conditionChange = previousDiary
                .filter(value -> diary.getConditionScore() != null && value.getConditionScore() != null)
                .map(value -> diary.getConditionScore().subtract(value.getConditionScore()))
                .orElse(null);

        return new DiaryResponse(date, diary.getMemo(), diary.getConditionScore(), conditionChange,
                diary.getWeightKg(), diary.getWaterMl(), diary.getSkinCondition(), diary.getMenstrualStatus(),
                totalCalories, calorieChange, MVP_RECOMMENDED_CALORIES, MVP_RECOMMENDED_CALORIES - totalCalories,
                totalActivity, activityChange,
                meals.stream().mapToInt(Meal::getCarbs).sum(),
                meals.stream().mapToInt(Meal::getProtein).sum(),
                meals.stream().mapToInt(Meal::getFat).sum(),
                meals.stream().map(MealResponse::from).toList(),
                activities.stream().map(ActivityResponse::from).toList());
    }

    public DailyDiaryResponse getDaily(Long userId, LocalDate date) {
        Diary diary = findDiary(userId, date);
        return toDailyResponse(diary, get(userId, date));
    }

    @Transactional
    public DailyDiaryResponse patchDaily(Long userId, DailyDiaryPatchRequest request) {
        if (request.periodStart() != null && request.periodEnd() != null
                && request.periodStart().isAfter(request.periodEnd())) {
            throw new BusinessException(ErrorCode.DATE_RANGE_INVALID);
        }
        Diary diary = getOrCreateDiary(userId, request.date());
        String skinConditions = request.skin() == null ? null : String.join(",", request.skin());
        diary.patchDaily(request.weightKg(), request.mood(), request.stress(), request.fatigue(),
                request.waterMl(), skinConditions, request.periodStart(), request.periodEnd(), request.note());
        return toDailyResponse(diary, get(userId, request.date()));
    }

    public List<DiaryHistoryItem> getHistory(Long userId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException(ErrorCode.DATE_RANGE_INVALID);
        }
        return diaryRepository.findAllByUserIdAndDateBetweenOrderByDateAsc(userId, from, to).stream()
                .map(diary -> {
                    List<Meal> meals = mealRepository.findAllByDiaryIdOrderByIdAsc(diary.getId());
                    List<Activity> activities = activityRepository.findAllByDiaryIdOrderByIdAsc(diary.getId());
                    return new DiaryHistoryItem(diary.getDate(), diary.getWeightKg(), diary.getMood(),
                            diary.getWaterMl(), meals.stream().mapToInt(Meal::getCalories).sum(),
                            activities.stream().mapToInt(Activity::getActivityAmount).sum());
                }).toList();
    }

    @Transactional
    public DiaryResponse save(Long userId, LocalDate date, DiarySaveRequest request) {
        Diary diary = diaryRepository.findByUserIdAndDate(userId, date)
                .orElseGet(() -> new Diary(findUser(userId), date));
        diary.update(request.memo(), request.condition(), request.weight(), request.waterIntake(),
                request.skinCondition(), request.menstrualStatus());
        diaryRepository.save(diary);
        return get(userId, date);
    }

    @Transactional
    public MealResponse createMeal(Long userId, LocalDate date, MealRequest request) {
        Diary diary = getOrCreateDiary(userId, date);
        return MealResponse.from(mealRepository.save(new Meal(diary, request.mealType(), request.foodName(),
                request.calories(), request.carbs(), request.protein(), request.fat())));
    }

    @Transactional
    public MealResponse updateMeal(Long userId, Long mealId, MealRequest request) {
        Meal meal = mealRepository.findByIdAndDiaryUserId(mealId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEAL_NOT_FOUND));
        meal.update(request.mealType(), request.foodName(), request.calories(), request.carbs(), request.protein(), request.fat());
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
        return ActivityResponse.from(activityRepository.save(new Activity(diary, request.activityAmount(), request.memo())));
    }

    @Transactional
    public ActivityResponse updateActivity(Long userId, Long activityId, ActivityRequest request) {
        Activity activity = activityRepository.findByIdAndDiaryUserId(activityId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVITY_NOT_FOUND));
        activity.update(request.activityAmount(), request.memo());
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
        List<String> skin = diary.getSkinConditions() == null || diary.getSkinConditions().isBlank()
                ? List.of() : List.of(diary.getSkinConditions().split(","));
        return new DailyDiaryResponse(diary.getDate(), diary.getWeightKg(), diary.getMood(),
                diary.getStress(), diary.getFatigue(), diary.getWaterMl(), skin,
                diary.getPeriodStart(), diary.getPeriodEnd(), diary.getMemo(),
                legacy.totalCalories(), legacy.calorieChange(), legacy.recommendedCalories(),
                legacy.remainingCalories(), legacy.totalActivity(), legacy.activityChange(),
                legacy.meals(), legacy.activities());
    }
}
