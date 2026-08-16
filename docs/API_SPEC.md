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

- AI 식단 분석 모델·타임아웃·재시도와 최소 개인화 정보
- 눈바디 파일 업로드 저장소와 AI 예상 이미지 생성
- 추천 시술의 필드와 추천 기준
