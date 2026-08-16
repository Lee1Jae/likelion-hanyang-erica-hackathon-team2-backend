package com.bloom.backend.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    COMMON_INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요."),
    DATE_INVALID(HttpStatus.BAD_REQUEST, "올바른 날짜 형식이 아닙니다."),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다."),
    AUTH_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 날짜의 기록이 없습니다."),
    MEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "식단 기록을 찾을 수 없습니다."),
    ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "활동 기록을 찾을 수 없습니다."),
    RESOURCE_FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
