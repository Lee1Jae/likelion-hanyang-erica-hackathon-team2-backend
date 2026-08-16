# BLOOM AI API 명세 초안

AI 기능 구현 전 프론트·백엔드·AI 담당자가 합의해야 할 초안입니다. 공개 API와 내부 AI API를 분리합니다.

## 원칙

- 프론트는 AI 서버를 직접 호출하지 않고 백엔드 공개 API만 호출합니다.
- AI 결과는 확정 데이터가 아닌 `DRAFT`로 저장합니다.
- 사용자가 음식명·칼로리·탄단지를 수정한 뒤 `CONFIRMED`로 확정합니다.
- 영양소를 추론하지 못하면 0이 아닌 null을 반환합니다.
- 모든 분석 결과에 `analysisId`, `status`, `modelVersion`을 남깁니다.
- AI 실패가 일반 식단·눈바디 사진 기록 기능을 막지 않아야 합니다.

## 1. 프론트 ↔ 백엔드 공개 API

### 식단 분석 요청

```http
POST /api/v1/ai/nutrition/analyses
Content-Type: multipart/form-data
```

| 필드 | 형식 | 필수 | 설명 |
| --- | --- | --- | --- |
| `date` | date | O | 식사 기록일 |
| `mealType` | enum | O | BREAKFAST/LUNCH/DINNER/SNACK |
| `image` | file | 조건부 | 사진 또는 text 중 하나 이상 |
| `text` | string | 조건부 | 사진 또는 text 중 하나 이상 |

사진과 텍스트를 함께 보내면 둘 다 분석 근거로 사용합니다.

```json
{
  "analysisId": 127,
  "status": "DRAFT",
  "modelVersion": "nutrition-v1",
  "foods": [
    {
      "draftFoodId": 301,
      "foodName": "현미밥",
      "estimatedAmount": "1공기",
      "kcal": 310,
      "carbs": 66,
      "protein": 6,
      "fat": 2,
      "confidence": 0.91
    }
  ],
  "totalKcal": 310
}
```

### 분석 초안 조회

```http
GET /api/v1/ai/nutrition/analyses/{analysisId}
```

### 분석 음식 수정

```http
PATCH /api/v1/ai/nutrition/analyses/{analysisId}/foods/{draftFoodId}
```

```json
{
  "foodName": "현미밥 반 공기",
  "kcal": 155,
  "carbs": 33,
  "protein": 3,
  "fat": 1
}
```

### 분석 확정

```http
POST /api/v1/ai/nutrition/analyses/{analysisId}/confirm
```

확정 시 일반 식단 `Meal`로 저장하며 일일 합계에 반영합니다.

### 분석 취소

```http
DELETE /api/v1/ai/nutrition/analyses/{analysisId}
```

### 눈바디 예상 이미지 요청

```http
POST /api/v1/care/body-checks/{bodyCheckId}/analysis
```

응답 상태는 `ANALYZING`, `COMPLETED`, `FAILED`입니다. 결과는 기존 눈바디 조회의 `expectedImageUrl`에서 확인합니다.

### AI 채팅

```http
POST /api/v1/ai/chat/sessions
POST /api/v1/ai/chat/sessions/{sessionId}/messages
GET  /api/v1/ai/chat/sessions/{sessionId}/messages
```

## 2. 백엔드 ↔ AI 서버 내부 API

AI 서버 주소와 인증 방식은 배포 환경에서만 관리합니다.

| Method | Endpoint | 용도 |
| --- | --- | --- |
| POST | `/internal/v1/nutrition/analyze` | 사진·텍스트 음식/영양소 추론 |
| POST | `/internal/v1/body-prediction` | 눈바디 예상 이미지 생성 |
| POST | `/internal/v1/chat/respond` | 챗봇 응답 생성 |
| POST | `/internal/v1/procedures/recommend` | 시술 추천 후보 생성 |

내부 응답 공통 필드:

```json
{
  "requestId": "uuid",
  "status": "SUCCEEDED",
  "modelVersion": "model-name-or-version",
  "result": {},
  "error": null
}
```

## 3. AI 공통 상태

| 상태 | 설명 |
| --- | --- |
| `PROCESSING` | 분석 진행 중 |
| `DRAFT` | 분석 완료, 사용자 확인 전 |
| `CONFIRMED` | 사용자 수정·확정 완료 |
| `FAILED` | 분석 실패, 직접 입력 가능 |
| `CANCELLED` | 사용자가 분석 초안 취소 |

## 4. 구현 전 추가 합의

- 동기 응답과 비동기 polling 중 선택
- 이미지 형식, 최대 크기, 보관 기간
- 사용자별 일일 요청 제한
- AI 분석 타임아웃과 재시도 횟수
- confidence를 사용자에게 표시할지 여부
- 챗봇 대화 보관 기간과 개인정보 제거 정책
- 모델 제공자와 비용 제한
