package com.bloom.backend.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    COMMON_INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값을 확인해 주세요."),
    DATE_INVALID(HttpStatus.BAD_REQUEST, "올바른 날짜 형식이 아닙니다."),
    DATE_RANGE_INVALID(HttpStatus.BAD_REQUEST, "조회 시작일과 종료일을 확인해 주세요."),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다."),
    AUTH_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 날짜의 기록이 없습니다."),
    MEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "식단 기록을 찾을 수 없습니다."),
    ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "활동 기록을 찾을 수 없습니다."),
    BODY_CHECK_NOT_FOUND(HttpStatus.NOT_FOUND, "눈바디 기록을 찾을 수 없습니다."),
    BODY_CHECK_PATCH_EMPTY(HttpStatus.BAD_REQUEST, "수정할 눈바디 정보를 한 개 이상 입력해 주세요."),
    BODY_CHECK_ANALYSIS_REMOVED(HttpStatus.GONE, "눈바디 예상 이미지 생성 기능은 제공하지 않습니다."),
    PERIOD_NOT_FOUND(HttpStatus.NOT_FOUND, "생리 기록을 찾을 수 없습니다."),
    PERIOD_PATCH_EMPTY(HttpStatus.BAD_REQUEST, "수정할 생리 기록을 한 개 이상 입력해 주세요."),
    IMAGE_EMPTY(HttpStatus.BAD_REQUEST, "이미지 파일을 선택해 주세요."),
    IMAGE_TYPE_UNSUPPORTED(HttpStatus.BAD_REQUEST, "JPEG, PNG, WebP 이미지만 업로드할 수 있습니다."),
    IMAGE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "이미지는 최대 10MB까지 업로드할 수 있습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),
    AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI 기능을 준비 중입니다. 잠시 후 다시 시도해 주세요."),
    AI_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "AI 리포트를 찾을 수 없습니다."),
    AI_CONVERSATION_NOT_FOUND(HttpStatus.NOT_FOUND, "AI 대화를 찾을 수 없습니다."),
    NUTRITION_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "식단 분석을 찾을 수 없습니다."),
    NUTRITION_DRAFT_FOOD_NOT_FOUND(HttpStatus.NOT_FOUND, "분석된 음식 항목을 찾을 수 없습니다."),
    NUTRITION_INPUT_INVALID(HttpStatus.BAD_REQUEST, "사진 또는 텍스트 중 선택한 입력 하나만 보내 주세요."),
    NUTRITION_ANALYSIS_STATE_INVALID(HttpStatus.CONFLICT, "현재 상태에서는 식단 분석을 변경할 수 없습니다."),
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
