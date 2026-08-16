package com.bloom.backend.user.service;

import com.bloom.backend.auth.repository.RefreshTokenRepository;
import com.bloom.backend.diary.repository.ActivityRepository;
import com.bloom.backend.diary.repository.DiaryRepository;
import com.bloom.backend.diary.repository.MealRepository;
import com.bloom.backend.global.error.BusinessException;
import com.bloom.backend.global.error.ErrorCode;
import com.bloom.backend.user.domain.User;
import com.bloom.backend.user.domain.UserProfile;
import com.bloom.backend.user.dto.OnboardingRequest;
import com.bloom.backend.user.dto.ProfilePatchRequest;
import com.bloom.backend.user.dto.ProfileResponse;
import com.bloom.backend.user.repository.UserProfileRepository;
import com.bloom.backend.user.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MealRepository mealRepository;
    private final ActivityRepository activityRepository;
    private final DiaryRepository diaryRepository;

    public UserService(UserRepository userRepository, UserProfileRepository profileRepository,
                       RefreshTokenRepository refreshTokenRepository, MealRepository mealRepository,
                       ActivityRepository activityRepository, DiaryRepository diaryRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.mealRepository = mealRepository;
        this.activityRepository = activityRepository;
        this.diaryRepository = diaryRepository;
    }

    @Transactional
    public ProfileResponse onboarding(Long userId, OnboardingRequest request) {
        User user = findUser(userId);
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseGet(() -> new UserProfile(user));
        profile.completeOnboarding(request.birthDate(), request.deliveryDate(), request.heightCm(), request.weightKg(),
                join(request.beautyGoals()), join(request.healthIssues()), request.lastPeriodDate(), request.cycleLength());
        profileRepository.save(profile);
        return response(user, profile);
    }

    public ProfileResponse getProfile(Long userId) {
        User user = findUser(userId);
        return response(user, findProfile(userId));
    }

    @Transactional
    public ProfileResponse patchProfile(Long userId, ProfilePatchRequest request) {
        User user = findUser(userId);
        UserProfile profile = findProfile(userId);
        profile.update(request.heightCm(), request.weightKg(),
                request.beautyGoals() == null ? null : join(request.beautyGoals()),
                request.healthIssues() == null ? null : join(request.healthIssues()),
                request.lastPeriodDate(), request.cycleLength());
        return response(user, profile);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findUser(userId);
        activityRepository.deleteAllByDiaryUserId(userId);
        mealRepository.deleteAllByDiaryUserId(userId);
        diaryRepository.deleteAllByUserId(userId);
        refreshTokenRepository.deleteAllByUserId(userId);
        profileRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private UserProfile findProfile(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private ProfileResponse response(User user, UserProfile profile) {
        return new ProfileResponse(user.getId(), user.getEmail(), user.getNickname(), profile.getBirthDate(),
                profile.getDeliveryDate(), profile.getHeightCm(), profile.getWeightKg(), split(profile.getBeautyGoals()),
                split(profile.getHealthIssues()), profile.getLastPeriodDate(), profile.getCycleLength(),
                profile.isOnboardingCompleted());
    }

    private String join(List<String> values) {
        return values == null || values.isEmpty() ? null : String.join(",", values);
    }

    private List<String> split(String values) {
        return values == null || values.isBlank() ? List.of() : Arrays.asList(values.split(","));
    }
}
