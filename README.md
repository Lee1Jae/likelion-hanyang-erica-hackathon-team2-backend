# Bloom Backend

![Java](https://img.shields.io/badge/Java-21-007396)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-6DB33F)
![License](https://img.shields.io/badge/visibility-public-blue)

산후 건강·바디케어 앱의 해커톤 MVP 백엔드입니다. 프론트 화면에서 사용하는 필드명을 API 계약의 기준으로 삼습니다.

> 현재 `main`: 인증, 다이어리, 식단·활동 CRUD와 일일 계산 API 구현 완료. 온보딩·기간 조회·홈·AI는 구현 예정입니다.

## 빠른 시작

필요 환경: Java 21, Docker Desktop

```bash
docker compose up -d
./gradlew bootRun
```

| 용도 | 주소 |
| --- | --- |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| Health check | `http://localhost:8080/actuator/health` |

기본 로컬 DB 계정은 `compose.yml`과 일치합니다. 운영 환경에서는 `.env.example`을 참고해 비밀값을 환경변수로 주입하며 실제 `.env`는 커밋하지 않습니다.

## 현재 구현 범위

- Java 21 호환, Spring Boot 3.5.5, Gradle 8.14.3
- MySQL 8.4, Spring Data JPA, Flyway
- 회원가입, 로그인, JWT Access Token
- Refresh Token rotation과 로그아웃 폐기
- 날짜별 다이어리 저장·조회
- 식단·활동 생성·수정·삭제
- 총/잔여 칼로리, 영양소, 활동량 및 전일 대비 변화 계산
- Bean Validation과 공통 오류 응답
- Swagger/OpenAPI와 Actuator health
- H2 기반 통합 테스트

## API 구현 상태

기본 경로는 `/api/v1`입니다. 로그인·회원가입·재발급 외 API에는 `Authorization: Bearer <accessToken>`이 필요합니다.

| 도메인 | Method | Endpoint | 상태 |
| --- | --- | --- | --- |
| 인증 | POST | `/auth/signup` | ✅ 완료 |
| 인증 | POST | `/auth/login` | ✅ 완료 |
| 인증 | POST | `/auth/reissue` | ✅ 완료 |
| 인증 | POST | `/auth/logout` | ✅ 완료 |
| 다이어리 | GET | `/diaries/{date}` | ✅ 완료 |
| 다이어리 | PUT | `/diaries/{date}` | ✅ 완료 |
| 식단 | POST | `/diaries/{date}/meals` | ✅ 완료 |
| 식단 | PATCH / DELETE | `/meals/{mealId}` | ✅ 완료 |
| 활동 | POST | `/diaries/{date}/activities` | ✅ 완료 |
| 활동 | PATCH / DELETE | `/activities/{activityId}` | ✅ 완료 |
| 사용자 | PUT / GET / PATCH | `/users/me/...` | 🟡 예정 |
| 달력 | GET | `/diaries?from=...&to=...` | 🟡 예정 |
| 홈·AI | - | `/home`, `/ai/...` | 🟡 예정 |

## ERD

현재 MVP에서 확정되어 실제 DB와 코드에 반영된 객체만 표시합니다.

```mermaid
erDiagram
    USERS ||--|| USER_PROFILES : has
    USERS ||--o{ REFRESH_TOKENS : owns
    USERS ||--o{ DIARIES : records
    DIARIES ||--o{ MEALS : contains
    DIARIES ||--o{ ACTIVITIES : contains

    USERS {
        BIGINT id PK
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR nickname
        VARCHAR role
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    USER_PROFILES {
        BIGINT id PK
        BIGINT user_id FK,UK
        DATE birth_date
        DATE delivery_date
        DECIMAL height_cm
        DECIMAL weight_kg
        DATE last_period_date
        INT cycle_length
        TEXT beauty_goals
        TEXT health_issues
        BOOLEAN onboarding_completed
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    REFRESH_TOKENS {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR token_hash UK
        TIMESTAMP expires_at
        TIMESTAMP revoked_at
        TIMESTAMP created_at
    }

    DIARIES {
        BIGINT id PK
        BIGINT user_id FK
        DATE record_date UK
        VARCHAR memo
        DECIMAL condition_score
        DECIMAL weight_kg
        INT water_ml
        VARCHAR skin_condition
        BOOLEAN menstrual_status
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    MEALS {
        BIGINT id PK
        BIGINT diary_id FK
        VARCHAR meal_type
        VARCHAR food_name
        INT calories
        INT carbs
        INT protein
        INT fat
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    ACTIVITIES {
        BIGINT id PK
        BIGINT diary_id FK
        INT activity_amount
        VARCHAR memo
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
```

- `USERS` ↔ `USER_PROFILES`: 사용자 한 명당 프로필 하나
- `USERS` ↔ `REFRESH_TOKENS`: 사용자 한 명이 로그인 세션별 Refresh Token을 여러 개 보유 가능
- `USERS` ↔ `DIARIES`: 사용자별 하루 한 개의 다이어리
- `DIARIES` ↔ `MEALS`, `ACTIVITIES`: 하루 기록에 식단과 활동을 여러 개 저장

## 프론트 JSON 계약

API 요청·응답 키는 프론트 화면의 명칭을 그대로 사용합니다. DB와 Java 내부에서는 단위를 명확히 하기 위해 `condition_score`, `weight_kg`, `water_ml` 같은 이름을 사용합니다.

| 의미 | JSON 필드 | DB/내부 필드 |
| --- | --- | --- |
| 컨디션 | `condition` | `conditionScore` |
| 체중(kg) | `weight` | `weightKg` |
| 수분 섭취량(ml) | `waterIntake` | `waterMl` |
| 피부 상태 | `skinCondition` | `skinCondition` |
| 생리 여부 | `menstrualStatus` | `menstrualStatus` |
| 활동량 | `activityAmount` | `activityAmount` |

`GET /api/v1/diaries/2026-08-14` 응답 예시:

```json
{
  "date": "2026-08-14",
  "memo": "좋음",
  "condition": 4.5,
  "conditionChange": 0.5,
  "weight": 61.8,
  "waterIntake": 1500,
  "skinCondition": "DRY",
  "menstrualStatus": false,
  "totalCalories": 320,
  "calorieChange": 120,
  "recommendedCalories": 2000,
  "remainingCalories": 1680,
  "totalActivity": 8200,
  "activityChange": -500,
  "carbs": 60,
  "protein": 8,
  "fat": 3,
  "meals": [],
  "activities": []
}
```

- 전날 다이어리가 없으면 `conditionChange`, `calorieChange`, `activityChange`는 `null`
- `remainingCalories = recommendedCalories - totalCalories`
- MVP의 `recommendedCalories`는 임시 기본값 `2000`; 개인화 산식 합의 후 교체

## 테스트

```bash
./gradlew clean test bootJar
```

현재 통합 테스트는 인증 흐름, Validation 실패, 다이어리 저장, 식단·활동 기록, 일일 계산 응답을 검증합니다.

## 프로젝트 구조

```text
src/main/java/com/bloom/backend
├── auth       # JWT 인증과 Refresh Token
├── diary      # 다이어리·식단·활동과 계산
├── user       # 사용자·온보딩 프로필
└── global     # 설정, 공통 Entity, 예외 처리
```

DB 변경은 JPA 자동 생성이 아니라 `src/main/resources/db/migration`의 Flyway SQL로 관리합니다.

## 협업 규칙

- 외부 JSON 필드 변경은 프론트 타입, Swagger, README, MVP 명세를 함께 수정합니다.
- `skinCondition`, `menstrualStatus`, `activityAmount` 정책은 팀 합의 전 추가 확장하지 않습니다.
- 계산값은 DB에 중복 저장하지 않고 조회 시 계산합니다.
- `main`에는 테스트와 `bootJar` 생성이 통과한 코드만 반영합니다.
- 자세한 브랜치·PR·계약 변경 절차는 [CONTRIBUTING.md](CONTRIBUTING.md)를 따릅니다.

## 다음 구현

1. 온보딩·프로필 API
2. 달력 기간 조회 API
3. 홈 통합 조회 API
4. AI 식단 분석·추천 API
