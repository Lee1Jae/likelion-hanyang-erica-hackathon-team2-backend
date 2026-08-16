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
  "skin": ["DRY", "SENSITIVE"],
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
| `skin` | string[] | X | 피부 태그, 별도 enum 합의 전 |
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

`mealType`은 `BREAKFAST`, `LUNCH`, `DINNER`, `SNACK` 중 하나입니다. AI 식단은 사진과 텍스트를 모두 지원하고 분석 초안을 사용자가 수정할 수 있게 만들 예정이며, 현재는 구현하지 않았습니다.

## 4. 눈바디 사진 기록

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
  "originalImageUrl": "https://cdn.example.com/body/original.jpg"
}
```

AI 예상 이미지는 후순위입니다. 응답의 `expectedImageUrl`은 구현 전까지 null이고 `analysisStatus`는 `NOT_REQUESTED`입니다.

## 5. 아직 확정하지 않은 계약

- 피부 상태 `skin` enum
- AI 식단 분석 요청·응답, DRAFT/CONFIRMED 상태
- 눈바디 파일 업로드 저장소와 AI 예상 이미지 생성
- 추천 시술의 필드와 추천 기준
