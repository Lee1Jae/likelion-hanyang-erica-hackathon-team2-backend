# BLOOM AI API 명세 초안

AI 기능 구현 전 프론트·백엔드·AI 담당자가 합의해야 할 초안입니다. 공개 API와 내부 AI API를 분리합니다.

## 원칙

- 프론트는 AI 서버를 직접 호출하지 않고 백엔드 공개 API만 호출합니다.
- AI 결과는 일반 식단에 반영하기 전 `DRAFT`(임시 분석 결과)로 저장합니다.
- 별도 확정 단계는 두지 않습니다. 사용자가 결과 화면에서 `기록하기`를 누르면 수정 여부와 관계없이 일반 식단으로 저장하고 분석 상태를 `RECORDED`로 변경합니다.
- 영양소를 추론하지 못하면 0이 아닌 null을 반환합니다.
- 사용자는 기록 전 음식 항목을 추가·수정·삭제할 수 있고, 기록 후에도 일반 식단 수정 API로 변경할 수 있습니다.
- 식약처 검색 결과가 없으면 AI 추정값을 사용하고 출처를 `AI_ESTIMATE`로 표시합니다.
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
| `inputType` | enum | O | IMAGE/TEXT |
| `image` | file | 조건부 | inputType=IMAGE일 때 필수 |
| `text` | string | 조건부 | inputType=TEXT일 때 필수 |

사진과 텍스트는 같은 분석 API를 사용하지만 한 요청에서 동시에 받지 않습니다. `inputType`에 해당하는 입력 하나만 전송합니다.

이미지는 JPEG, PNG, WebP만 지원하고 최대 10MB로 제한합니다. 서버는 EXIF를 제거하고 긴 변을 최대 2048px로 조정할 수 있습니다. 원본 사진은 기록일 또는 업로드일로부터 1년간 비공개 저장소에 보관하며, 정식 식단 저장 후에도 화면에 표시합니다.

```json
{
  "analysisId": 127,
  "status": "DRAFT",
  "modelVersion": "nutrition-v1",
  "foods": [
    {
      "draftFoodId": 301,
      "foodName": "현미밥",
      "amount": 150,
      "amountUnit": "g",
      "kcal": 310,
      "carbs": 66,
      "protein": 6,
      "fat": 2,
      "confidence": 0.91,
      "source": "MFDS"
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
  "amount": 75,
  "amountUnit": "g",
  "kcal": 155,
  "carbs": 33,
  "protein": 3,
  "fat": 1
}
```

`foodName`, `amount`, `amountUnit`, `kcal`, `carbs`, `protein`, `fat`은 수정할 수 있습니다. `kcal`과 탄수화물·단백질·지방은 알 수 없는 경우 null을 허용합니다.

### 분석 음식 추가

```http
POST /api/v1/ai/nutrition/analyses/{analysisId}/foods
```

직접 추가한 항목의 `source`는 `USER_INPUT`입니다.

### 분석 음식 삭제

```http
DELETE /api/v1/ai/nutrition/analyses/{analysisId}/foods/{draftFoodId}
```

### 식단으로 기록

```http
POST /api/v1/ai/nutrition/analyses/{analysisId}/record
```

별도 확정 과정은 없습니다. `기록하기`를 누르면 수정하지 않은 DRAFT도 일반 식단 `Meal`로 저장하고 상태를 `RECORDED`로 바꾸며 일일 합계에 반영합니다. 생성된 각 Meal은 이후 일반 식단 PATCH/DELETE API로 수정·삭제할 수 있습니다. 사진 분석이었다면 해당 사진 URL과 보관 만료일도 식단 기록에 연결합니다.

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
| POST | `/internal/v1/nutrition/analyze` | 사진 또는 텍스트 음식/영양소 추론 |
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
| `RECORDED` | 사용자가 기록하기를 눌러 일반 식단 저장 완료 |
| `FAILED` | 분석 실패, 직접 입력 가능 |
| `CANCELLED` | 사용자가 분석 초안 취소 |

AI가 음식을 하나도 인식하지 못하면 상태를 `FAILED`로 저장하고 `manualInputAvailable: true`를 반환하여 프론트가 직접 입력 화면으로 전환합니다. 식약처 후보가 여러 개면 AI가 음식명·분류·제조사·기준량을 비교해 가장 적합한 하나를 선택하고, 선택한 식품 코드와 출처를 분석 결과에 보존합니다. 식약처 결과가 없으면 분석을 실패시키지 않고 `AI_ESTIMATE`를 사용합니다.

## 4. 구현 전 추가 합의

- 동기 응답과 비동기 polling 중 선택
- 챗봇 대화 보관 기간과 개인정보 제거 정책
- 모델 제공자와 비용 제한

## 5. MVP 운영 정책

- 음식 분석 제한시간은 60초이며 일시적 네트워크·제공자 장애에 한해 1회 재시도합니다.
- 사용자별 일일 분석 횟수는 제한하지 않습니다. 단, 중복 클릭과 비정상적인 순간 요청을 막는 기술적 rate limit은 적용합니다.
- 기록되지 않은 DRAFT는 생성 7일 후 이미지와 함께 자동 삭제합니다.
- confidence 숫자는 사용자에게 직접 표시하지 않습니다. 신뢰도가 낮은 항목에만 `확인이 필요해요` 안내를 표시합니다.
- 식단 분석에는 음식 입력, 알레르기, 식단 목표, 목표 칼로리와 익명화한 사용자 식별값만 전달할 수 있습니다.
- 이름, 이메일, 전화번호, 주소, 정확한 생년월일, 출산일, 일기 원문, 감정·신체·피부 기록 전체는 식단 분석 모델에 전달하지 않습니다.
- 사용자가 식단 또는 계정을 삭제하면 1년 보관 정책보다 사용자 삭제 요청을 우선하여 연결된 사진도 삭제합니다.
- null인 kcal·영양소는 합계에서 제외하고, 응답에 영양정보가 불완전함을 알리는 별도 표시를 추가합니다.
