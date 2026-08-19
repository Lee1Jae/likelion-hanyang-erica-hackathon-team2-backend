# BLOOM MVP API 명세

이 문서는 프론트엔드–백엔드 합의 후 확정한 현재 API 계약입니다. JSON 필드명은 이 문서를 기준으로 사용합니다.

- Base URL: `/api/v1`
- 날짜: `YYYY-MM-DD`
- 인증 API를 제외한 요청: `Authorization: Bearer <accessToken>`
- 미기록 숫자·날짜: `null`
- 빈 태그 목록: `[]`

## 0. 온보딩·프로필 추가 필드

`POST /api/v1/onboarding`, `GET /api/v1/users/me/profile`, `PATCH /api/v1/users/me/profile`은 다음 배열을 공통으로 사용합니다.

```json
{
  "focusAreas": ["ABDOMEN", "THIGH"],
  "recoveryAreas": ["CORE", "PELVIS"],
  "skinConcerns": ["STRETCH_MARKS", "LOSS_OF_ELASTICITY"]
}
```

PATCH는 전달한 필드만 변경합니다. 빈 배열 `[]`을 전달하면 해당 목록을 비웁니다. 필드를 생략하면 기존 값을 유지합니다.

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

### 생리 기간 기록 CRUD

일일 기록의 `periodStart`, `periodEnd`와 별도로 캘린더에서 생리 기간 목록을 관리할 때 사용합니다.

```http
POST   /api/v1/periods
GET    /api/v1/periods
PATCH  /api/v1/periods/{periodId}
DELETE /api/v1/periods/{periodId}
```

```json
{
  "startDate": "2026-08-18",
  "endDate": "2026-08-22"
}
```

생성은 두 날짜가 모두 필수이고 `startDate <= endDate`여야 합니다. PATCH는 둘 중 변경할 값만 보내며,
빈 요청은 400입니다. GET은 시작일이 최근인 기록부터 배열로 반환하고, 본인 기록만 조회·변경·삭제할 수 있습니다.

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

`mealType`은 `BREAKFAST`, `LUNCH`, `DINNER`, `SNACK` 중 하나입니다. `kcal`, `carbs`, `protein`, `fat`은 알 수 없으면 null일 수 있습니다. 일일 합계는 null 항목을 제외해 계산하며 두 일일 조회 응답 모두 `nutritionIncomplete`로 일부 영양정보가 빠졌는지 알립니다.

AI 식단 분석은 다음 하나의 API에서 `inputType=IMAGE` 또는 `inputType=TEXT`로 구분하며, 사진과 텍스트를 동시에 보내지 않습니다.

```http
POST /api/v1/ai/nutrition/analyses
Content-Type: multipart/form-data
```

분석 결과는 `DRAFT`로 서버에 임시 저장됩니다. 사용자는 음식 항목을 추가·수정·삭제할 수 있고,
`POST /api/v1/ai/nutrition/analyses/{analysisId}/record`를 호출하면 별도 확정 단계 없이 일반 식단으로 저장됩니다.
기록된 식단 응답에는 `nutritionAnalysisId`, 사진 입력이면 `sourceImageUrl`이 포함되며 이후 일반 식단 PATCH/DELETE API로 변경할 수 있습니다.
영양값을 판단할 수 없으면 0을 만들지 않고 null로 반환합니다. 자세한 요청·응답은 `AI_API_DRAFT.md`를 따릅니다.

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

프론트는 먼저 `POST /api/v1/uploads/images`에 파일과 `purpose=BODY_CHECK`를 보내고,
응답의 인증 이미지 URL을 `originalImageUrl`로 저장합니다. 프론트가 `blob:` URL이나 로컬 경로를 눈바디 API에 직접 보내면 안 됩니다.

눈바디 예상 이미지 생성은 MVP 범위와 비용 정책에 따라 제거했습니다. 눈바디는 원본 사진과 촬영일을
기록·조회·수정·삭제하는 기능만 제공합니다. 기존 클라이언트와 DB 호환을 위해 응답의
`expectedImageUrl=null`, `analysisStatus=NOT_REQUESTED` 필드는 당분간 유지하지만,
`POST /api/v1/care/body-checks/{bodyCheckId}/analysis` 호출에는 과금 없이
`410 BODY_CHECK_ANALYSIS_REMOVED`를 반환합니다.

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

`createdAt`은 ISO-8601 UTC 시각입니다. 대화 목록은 최신 대화 순, 대화 상세의 메시지는 오래된 순으로 반환합니다. 다른 사용자의 대화 또는 존재하지 않는 대화는 정보 노출 방지를 위해 동일하게 `404 Not Found`로 처리합니다. 구체적인 목록·상세 응답과 실패 정책은 `AI_API_DRAFT.md`를 따릅니다. 대화·메시지 저장과 실제 모델 호출이 구현되어 있으며, 제공자 키가 없거나 호출이 실패하면 가짜 답변 대신 `503 AI_SERVICE_UNAVAILABLE`을 반환합니다.

## 6. AI 운영 계약

- 텍스트 기본 모델: `OPENAI_MODEL` 환경변수, 현재 기본값 `gpt-5.6-terra`
- 호출 방식: 채팅·식단·시술·리포트는 OpenAI Responses API를 사용하며 구조가 필요한 응답은 JSON Schema로 검증
- 제한시간: 연결 10초, 응답 120초
- 재시도: 네트워크·서버 장애에 한해 1회
- 개인정보: 이메일·닉네임·원본 생년월일은 모델에 보내지 않고 나이·산후 경과일과 필요한 건강 기록만 전달
- 실패 정책: 가짜 결과를 만들지 않고 503, 리포트는 `FAILED`, 식단은 직접 입력 가능 상태로 저장
- 미구현: 식약처 데이터 API 연결

## 7. 이미지 업로드

`POST /api/v1/uploads/images`는 `multipart/form-data`로 `file`과 `purpose=BODY_CHECK`를 받습니다. JPEG, PNG, WebP만 허용하며 최대 크기는 10MB입니다.

```json
{
  "imageUrl": "https://{backend}/api/v1/uploads/images/1",
  "contentType": "image/jpeg",
  "size": 1839281
}
```

이미지는 MVP에서 MySQL에 비공개 저장되며 `GET /api/v1/uploads/images/{imageId}`도 Bearer Access Token이 필요합니다. 따라서 프론트는 `imageUrl`을 인증 헤더로 `fetch`한 후 응답 Blob을 `URL.createObjectURL`로 변환해 표시합니다. 업로드 URL을 `POST /api/v1/care/body-checks`의 `originalImageUrl`로 저장합니다.

배포 환경은 `PUBLIC_BASE_URL`을 외부 HTTPS 백엔드 주소로 설정합니다. 이 값을 기준으로 업로드 URL을
생성하므로 HTTPS 프론트에서 Mixed Content가 발생하지 않습니다. 값이 없으면 요청 주소와 프록시 전달
헤더를 기준으로 URL을 생성합니다.

## 8. AI 추천 식단·추천 시술·리포트

```http
POST /api/v1/ai/meals/recommendations
POST /api/v1/ai/procedures/recommendations
POST /api/v1/ai/reports
GET  /api/v1/ai/reports/{reportId}
GET  /api/v1/ai/reports/latest
```

추천 식단은 프론트가 `date`와 `mealType`만 보내며, 백엔드가 로그인 사용자의 프로필과 해당 날짜까지 최근 14일 식단·활동·컨디션·생리 기록을 조회해 모델에 전달합니다.

```json
{
  "date": "2026-08-19",
  "mealType": "DINNER"
}
```

응답은 `title`, `description`, `foods`, `totalKcal`, `totalCarbs`, `totalProtein`, `totalFat`, `reason`, `generatedAt`을 포함합니다. 음식별 값 하나라도 알 수 없으면 해당 영양소 총합도 `null`이며, 백엔드가 음식별 값을 합산해 총합을 계산합니다.

추천 시술은 `bodyCheckId`만 받고, 백엔드가 눈바디 원본 이미지와 프로필·미용 목표·건강정보를 조합해 모델에 전달합니다. 판단할 근거가 없는 가격·횟수·간격은 null입니다. 리포트 생성은 최대 31일 범위의 `from`, `to`를 받고 해당 기간의 컨디션 점수·태그, 식단 합계, 활동, 피부, 생리 기록을 취합합니다. 응답 계약은 `AI_API_DRAFT.md`를 따릅니다. `OPENAI_API_KEY`가 없거나 제공자 장애가 발생하면 가짜 결과 없이 `503 AI_SERVICE_UNAVAILABLE`, 조회할 리포트가 없으면 `404 AI_REPORT_NOT_FOUND`를 반환합니다.

## 9. 마일리지

```http
GET  /api/v1/mileage
GET  /api/v1/mileage/history
POST /api/v1/mileage/attendance
POST /api/v1/mileage/routine-streak/check
```

출석은 Asia/Seoul 기준 날짜마다 100점, 운동 연속 기록은 3일 100점·7일 300점·14일 500점입니다. 운동일은 `exerciseMinutes > 0` 또는 `burnedKcal > 0`인 Activity가 존재하는 날짜이며 오늘부터 연속된 일수를 계산합니다. `ATTENDANCE:{date}`, `ROUTINE_STREAK:{days}` 고유 참조값으로 중복 지급을 방지합니다. 스토어 구매 보상은 실제 주문 API가 없으므로 구현하지 않습니다.
