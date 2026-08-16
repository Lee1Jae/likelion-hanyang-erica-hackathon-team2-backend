# BLOOM MVP API 명세

이 문서는 프론트엔드–백엔드 합의 후 확정한 현재 API 계약입니다. JSON 필드명은 이 문서를 기준으로 사용합니다.

- Base URL: `/api/v1`
- 날짜: `YYYY-MM-DD`
- 인증 API를 제외한 요청: `Authorization: Bearer <accessToken>`
- 미기록 숫자·날짜: `null`
- 빈 태그 목록: `[]`

## 1. 일일 컨디션·생활 기록

### 조회

```http
GET /api/v1/diary/daily?date=2026-08-16
```

`date`를 생략하면 Asia/Seoul 기준 오늘을 조회합니다.

### 부분 저장

```http
PATCH /api/v1/diary/daily
Content-Type: application/json
```

```json
{
  "date": "2026-08-16",
  "weightKg": 61.2,
  "emotionScore": 5,
  "bodyScore": 3,
  "emotionTags": ["HAPPY", "STRESS"],
  "bodyTags": ["MENSTRUATING", "LOWER_BACK_PAIN"],
  "waterMl": 1800,
  "skin": ["DRYNESS", "SENSITIVITY"],
  "periodStart": "2026-08-14",
  "periodEnd": "2026-08-18",
  "memo": "회복 중"
}
```

| 필드 | 형식 | 필수 | 규칙 |
| --- | --- | --- | --- |
| `date` | date | O | 기록일 |
| `weightKg` | decimal | X | 20.0~300.0 |
| `emotionScore` | integer | X | 0~5, 0은 나쁜 상태, 미기록은 null |
| `bodyScore` | integer | X | 0~5, 0은 나쁜 상태, 미기록은 null |
| `emotionTags` | enum[] | X | 감정 태그, 최대 16개 |
| `bodyTags` | enum[] | X | 생리주기·신체상태·식욕 태그, 최대 11개 |
| `waterMl` | integer | X | 0~10000ml |
| `skin` | SkinTag[] | X | 피부 상태 태그, 최대 12개 |
| `periodStart` | date | X | 생리 시작일 |
| `periodEnd` | date | X | 생리 종료일 |
| `memo` | string | X | 최대 1000자 |

PATCH는 전달한 필드만 변경합니다. `stress`, `fatigue`, `mood`, `note`는 사용하지 않습니다.

### 감정 태그

| 화면 표시 | API enum |
| --- | --- |
| 행복 | `HAPPY` |
| 기쁨 | `JOY` |
| 설렘 | `EXCITED` |
| 신남 | `ENERGETIC` |
| 평온 | `CALM` |
| 편안 | `COMFORTABLE` |
| 지루함 | `BORED` |
| 불안 | `ANXIOUS` |
| 불쾌 | `UNPLEASANT` |
| 불편 | `UNCOMFORTABLE` |
| 자책 | `SELF_BLAME` |
| 슬픔 | `SAD` |
| 짜증 | `IRRITATED` |
| 분노 | `ANGRY` |
| 예민 | `SENSITIVE` |
| 스트레스 | `STRESS` |

### 신체 태그

| 분류 | 화면 표시 | API enum |
| --- | --- | --- |
| 생리주기 | 생리중 | `MENSTRUATING` |
| 생리주기 | 가임기 | `FERTILE_WINDOW` |
| 생리주기 | 배란기 | `OVULATION` |
| 신체상태 | 피곤함 | `FATIGUED` |
| 신체상태 | 붓기 | `SWELLING` |
| 신체상태 | 허리 통증 | `LOWER_BACK_PAIN` |
| 신체상태 | 골반 통증 | `PELVIC_PAIN` |
| 신체상태 | 근육통 | `MUSCLE_PAIN` |
| 식욕 | 식욕 저하 | `LOW_APPETITE` |
| 식욕 | 식욕 보통 | `NORMAL_APPETITE` |
| 식욕 | 식욕 증가 | `INCREASED_APPETITE` |

### 피부 상태 태그

| 화면 표시 | API enum |
| --- | --- |
| 여드름 | `ACNE` |
| 기미 | `MELASMA` |
| 색소침착 | `HYPERPIGMENTATION` |
| 건조함 | `DRYNESS` |
| 민감함 | `SENSITIVITY` |
| 홍조 | `REDNESS` |
| 가려움 | `ITCHING` |
| 튼살 | `STRETCH_MARKS` |
| 탄력 저하 | `LOSS_OF_ELASTICITY` |
| 흉터 | `SCARRING` |
| 유분 증가 | `OILINESS` |
| 넓어진 모공 | `ENLARGED_PORES` |

## 2. 활동 기록

```http
POST /api/v1/diaries/{date}/activities
PATCH /api/v1/activities/{activityId}
DELETE /api/v1/activities/{activityId}
```

```json
{
  "steps": 6500,
  "exerciseMinutes": 25,
  "burnedKcal": 160,
  "memo": "걷기"
}
```

| 필드 | 형식 | 규칙 |
| --- | --- | --- |
| `steps` | integer | 0~200000 |
| `exerciseMinutes` | integer | 0~1440분 |
| `burnedKcal` | integer | 0~10000kcal |
| `memo` | string | 최대 200자 |

일일 조회 응답은 `totalSteps`, `stepsChange`, `totalExerciseMinutes`, `exerciseMinutesChange`, `totalBurnedKcal`, `burnedKcalChange`를 제공합니다.

## 3. 식단 기록

```http
POST /api/v1/diaries/{date}/meals
PATCH /api/v1/meals/{mealId}
DELETE /api/v1/meals/{mealId}
```

```json
{
  "mealType": "LUNCH",
  "foodName": "현미밥",
  "kcal": 320,
  "carbs": 60,
  "protein": 8,
  "fat": 3
}
```

`mealType`은 `BREAKFAST`, `LUNCH`, `DINNER`, `SNACK` 중 하나입니다. `kcal`, `carbs`, `protein`, `fat`은 알 수 없으면 null일 수 있습니다. 일일 합계는 null 항목을 제외해 계산하며 두 일일 조회 응답 모두 `nutritionIncomplete`로 일부 영양정보가 빠졌는지 알립니다. AI 식단 분석은 사진과 텍스트를 하나의 API에서 서로 다른 요청으로 지원합니다. 분석 결과는 DRAFT로 임시 저장되고 음식 추가·수정·삭제 후 `기록하기`를 누르면 별도 확정 단계 없이 일반 식단으로 저장됩니다. 기록 후에도 일반 식단 수정 API로 변경할 수 있습니다. 사진 입력은 정식 식단에 연결하여 1년간 표시합니다. 현재 AI 분석은 구현 전입니다.

## 4. 눈바디 사진 기록

눈바디 API는 이미지 파일 자체가 아니라 이미지 저장소에서 발급받은 URL을 저장합니다. 모든 API는 `Authorization: Bearer {accessToken}` 헤더가 필요합니다.

```http
POST   /api/v1/care/body-checks
GET    /api/v1/care/body-checks
GET    /api/v1/care/body-checks/{bodyCheckId}
PATCH  /api/v1/care/body-checks/{bodyCheckId}
DELETE /api/v1/care/body-checks/{bodyCheckId}
```

```json
{
  "recordedDate": "2026-08-16",
  "originalImageUrl": "https://cdn.example.com/body.jpg"
}
```

### 생성 요청

`POST /api/v1/care/body-checks`

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `recordedDate` | `string(date)` | O | 사진 촬영 날짜, `YYYY-MM-DD` |
| `originalImageUrl` | `string` | O | 업로드가 끝난 원본 이미지의 HTTP(S) URL, 최대 1,000자 |

성공 시 `201 Created`와 생성된 `BodyCheckResponse`를 반환합니다. 프론트는 먼저 협의된 이미지 저장소에 파일을 업로드하고, 받은 URL을 이 API로 전달합니다.

### 수정 요청

`PATCH /api/v1/care/body-checks/{bodyCheckId}`

```json
{
  "recordedDate": "2026-08-15",
  "originalImageUrl": "https://cdn.example.com/body-new.jpg"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `recordedDate` | `string(date)` | X | 변경할 촬영 날짜 |
| `originalImageUrl` | `string` | X | 변경할 HTTP(S) 이미지 URL, 최대 1,000자 |

변경할 필드만 전송합니다. 단, 두 필드가 모두 빠진 빈 요청 `{}`은 `400 Bad Request`입니다. 성공 시 `200 OK`와 수정된 `BodyCheckResponse`를 반환합니다.

### 이미지 업로드 책임

현재 백엔드는 이미지 URL만 저장하며 파일 업로드 API는 제공하지 않습니다. 이미지 저장소, 업로드 주체, URL 발급 방식, 파일 크기·형식·보관 정책은 프론트·백엔드가 별도로 확정해야 합니다. 이미지 저장소가 정해진 뒤에는 허용 호스트 검증 또는 백엔드 발급 업로드 URL 방식으로 보강합니다.

AI 예상 이미지는 후순위입니다. 응답의 `expectedImageUrl`은 구현 전까지 null이고 `analysisStatus`는 `NOT_REQUESTED`입니다.

## 5. AI 채팅 계약

```http
POST /api/v1/ai/chat
GET  /api/v1/ai/conversations
GET  /api/v1/ai/conversations/{conversationId}
```

모든 요청에 Bearer Access Token이 필요합니다. 새 대화는 `conversationId` 없이 메시지를 보내고, 기존 대화는 `conversationId`를 함께 보냅니다.

```json
{
  "conversationId": 12,
  "message": "그럼 집에서 할 수 있는 운동으로 알려줘"
}
```

`conversationId`는 선택이며 `message`는 공백 제외 1~2,000자의 필수 문자열입니다. 첫 메시지 전송 시 대화를 자동 생성하고 사용자 메시지와 AI 답변을 서버에 저장합니다. 백엔드는 Access Token으로 사용자를 확인하고 프로필·최근 식단·활동·컨디션 중 필요한 최소 데이터만 조회하므로 프론트가 개인화 데이터를 반복 전송하지 않습니다.

```json
{
  "conversationId": 12,
  "answer": "오늘은 가벼운 걷기와 스트레칭을 추천해요.",
  "createdAt": "2026-08-16T11:10:00Z"
}
```

`createdAt`은 ISO-8601 UTC 시각입니다. 대화 목록은 최신 대화 순, 대화 상세의 메시지는 오래된 순으로 반환합니다. 다른 사용자의 대화 또는 존재하지 않는 대화는 정보 노출 방지를 위해 동일하게 `404 Not Found`로 처리합니다. 구체적인 목록·상세 응답과 실패 정책은 `AI_API_DRAFT.md`를 따릅니다. 현재 API 계약만 확정됐으며 AI 호출과 대화 저장 코드는 구현 전입니다.

## 6. 아직 확정하지 않은 계약

- AI 식단 분석 모델·타임아웃·재시도와 최소 개인화 정보
- 눈바디 파일 업로드 저장소와 AI 예상 이미지 생성
- 추천 시술의 필드와 추천 기준
