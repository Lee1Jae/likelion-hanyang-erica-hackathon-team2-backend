package com.bloom.backend.diary.dto;

import com.bloom.backend.diary.domain.Activity;

public record ActivityResponse(Long activityId, int steps, int exerciseMinutes, int burnedKcal, String memo) {
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(activity.getId(), activity.getSteps(), activity.getExerciseMinutes(),
                activity.getBurnedKcal(), activity.getMemo());
    }
}
