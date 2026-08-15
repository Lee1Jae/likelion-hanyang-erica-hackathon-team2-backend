# Bloom Backend

산후 건강·바디케어 앱의 해커톤 MVP 백엔드입니다. 프론트 JSON 계약과 독립적인 공통 기반부터 구현했습니다.

## 현재 구현 범위

- Java 21 호환, Spring Boot 3.5.5, Gradle 8.14.3
- MySQL 8.4, Spring Data JPA, Flyway
- 회원가입, 로그인, JWT Access Token
- Refresh Token rotation과 로그아웃 폐기
- Bean Validation과 공통 오류 응답
- Swagger/OpenAPI와 Actuator health
- H2 기반 통합 테스트

## 로컬 실행

```bash
docker compose up -d
./gradlew bootRun
```

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`

운영 환경에서는 `.env.example`을 참고해 모든 비밀값을 환경변수로 주입합니다. 실제 비밀값은 저장소에 커밋하지 않습니다.

## 테스트

```bash
./gradlew test
```

현재 통합 테스트는 회원가입 → 로그인 → 토큰 재발급 → 로그아웃과 Validation 실패 응답을 검증합니다.

## 다음 구현

팀에서 프론트 Response JSON을 합의한 뒤 아래 순서로 진행합니다.

1. 온보딩·프로필 API
2. 날짜 기반 다이어리
3. 식단·활동 CRUD
4. 일별 계산값
5. 홈·AI API
