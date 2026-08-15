package com.bloom.backend.auth.dto;

public record SignupResponse(Long userId, String email, String nickname) {
}
