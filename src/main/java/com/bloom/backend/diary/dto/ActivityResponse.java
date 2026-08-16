package com.bloom.backend.diary.dto;

import com.bloom.backend.diary.domain.Activity;

public record ActivityResponse(Long activityId, int activityAmount, String memo) {
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(activity.getId(), activity.getActivityAmount(), activity.getMemo());
    }
}
