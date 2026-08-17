package com.bloom.backend.mileage.dto;

public record MileageRewardResponse(boolean rewarded, int amount, int balance, String reason, Integer streak) {}
