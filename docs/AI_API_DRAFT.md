# BLOOM AI API 구현 명세

프론트가 호출하는 공개 계약과 현재 백엔드의 실제 AI 처리 방식을 정리합니다.

## 원칙

- 프론트는 AI 서버를 직접 호출하지 않고 백엔드 공개 API만 호출합니다.
- AI 결과는 일반 식단에 반영하기 전 `DRAFT`(임시 분석 결과)로 저장합니다.
- 별도 확정 단계는 두지 않습니다. 사용자가 결과 화면에서 `기록하기`를 누르면 수정 여부와 관계없이 일반 식단으로 저장하고 분석 상태를 `RECORDED`로 변경합니다.
- 영양소를 추론하지 못하면 0이 아닌 null을 반환합니다.
- 사용자는 기록 전 음식 항목을 추가·수정·삭제할 수 있고, 기록 후에도 일반 식단 수정 API로 변경할 수 있습니다.
- 현재는 AI 추정값을 `AI_ESTIMATE`, 사용자가 직접 추가한 값은 `USER_INPUT`으로 표시합니다.
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

이미지는 JPEG, PNG, WebP만 지원하고 최대 10MB로 제한합니다. 파일 확장자가 아닌 Content-Type과 실제 파일 시그니처를 함께 검사합니다. 원본 사진은 업로드일로부터 1년간 인증이 필요한 DB 저장소에 보관하고 만료 파일을 매일 삭제합니다. 정식 식단 응답의 `sourceImageUrl`로 계속 표시할 수 있습니다.

```json
{
  "analysisId": 127,
  "status": "DRAFT",
  "modelVersion": "gpt-5.6-terra",
  "imageUrl": null,
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
      "source": "AI_ESTIMATE"
    }
  ],
  "totalKcal": 310,
  "manualInputAvailable": false
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

### 추천 시술

```http
POST /api/v1/ai/procedures/recommendations
```

프론트는 `bodyCheckId`만 전송하고 백엔드가 로그인 사용자의 프로필·미용 목표·건강정보를 조합합니다. 추정할 수 없는 가격·횟수·간격은 0이나 임의 문구 대신 null입니다. AI 미연결 또는 제공자 장애 시 `503 AI_SERVICE_UNAVAILABLE`을 반환합니다.

### AI 리포트

```http
POST /api/v1/ai/reports
GET  /api/v1/ai/reports/{reportId}
GET  /api/v1/ai/reports/latest
```

생성 요청은 `from`, `to`만 받고 최대 31일 범위에서 백엔드가 프로필·컨디션·식단·활동·피부·생리 기록을 조회합니다. 상태는 `PROCESSING`, `COMPLETED`, `FAILED`를 사용합니다. 모델 키가 없거나 제공자 호출이 실패하면 503과 함께 실패 상태를 저장하며, 저장된 리포트가 없으면 404입니다.

### AI 채팅

```http
POST /api/v1/ai/chat
GET  /api/v1/ai/conversations
GET  /api/v1/ai/conversations/{conversationId}
```

모든 요청은 `Authorization: Bearer {accessToken}` 헤더가 필요합니다. 프론트는 프로필·식단·활동·컨디션 데이터를 매 요청에 싣지 않습니다. 백엔드가 인증 사용자 기준으로 필요한 최신 데이터를 조회하고, 허용된 최소 정보만 AI에 전달합니다.

#### 메시지 전송

새 대화는 `conversationId`를 생략합니다.

```json
{
  "message": "오늘 운동 뭐 하면 좋을까?"
}
```

기존 대화를 이어갈 때만 `conversationId`를 전달합니다.

```json
{
  "conversationId": 12,
  "message": "그럼 집에서 할 수 있는 운동으로 알려줘"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `conversationId` | `Long` | X | 기존 대화 ID. 새 대화는 생략 |
| `message` | `String` | O | 공백 제외 1~2,000자의 사용자 메시지 |

첫 메시지라면 대화를 자동 생성하고 사용자 메시지와 AI 답변을 모두 저장합니다. 기존 대화 ID는 반드시 현재 로그인 사용자의 것이어야 하며, 없거나 다른 사용자의 대화면 `404 Not Found`를 반환합니다.

```json
{
  "conversationId": 12,
  "answer": "오늘은 가벼운 걷기와 스트레칭을 추천해요.",
  "createdAt": "2026-08-16T11:10:00Z"
}
```

`createdAt`은 ISO-8601 UTC 시각입니다. AI 제공자 장애나 제한시간 초과 시 사용자 메시지는 실패 상태로 남기고 `503 Service Unavailable`을 반환하며, 임의의 가짜 답변은 저장하지 않습니다.

#### 대화 목록

`GET /api/v1/ai/conversations`

```json
[
  {
    "conversationId": 12,
    "title": "오늘 운동 추천",
    "lastMessageAt": "2026-08-16T11:10:00Z"
  }
]
```

최신 메시지가 있는 대화부터 반환합니다. MVP에서는 대화가 많지 않다는 전제로 전체 목록을 반환하며, 운영 전 페이지네이션을 추가합니다.

#### 대화 상세

`GET /api/v1/ai/conversations/{conversationId}`

```json
{
  "conversationId": 12,
  "title": "오늘 운동 추천",
  "messages": [
    {
      "role": "USER",
      "content": "오늘 운동 뭐 하면 좋을까?",
      "createdAt": "2026-08-16T11:09:55Z"
    },
    {
      "role": "ASSISTANT",
      "content": "오늘은 가벼운 걷기와 스트레칭을 추천해요.",
      "createdAt": "2026-08-16T11:10:00Z"
    }
  ]
}
```

메시지는 오래된 순서로 반환하며 `role`은 `USER`, `ASSISTANT` 중 하나입니다.

## 2. 백엔드 ↔ AI 제공자

백엔드는 프론트 요청을 인증한 후 텍스트 기능을 OpenAI Responses API로 호출합니다. API 키는 응답이나 저장소에 노출하지 않고 배포 환경변수로만 관리합니다. 눈바디 예상 이미지 생성 기능은 MVP 범위에서 제거했습니다.

| 설정 | 환경변수 | 기본값 |
| --- | --- | --- |
| API 키 | `OPENAI_API_KEY` | 없음 |
| 모델 | `OPENAI_MODEL` | `gpt-5.6-terra` |
| Base URL | `OPENAI_BASE_URL` | `https://api.openai.com/v1` |
| 연결 제한시간 | `OPENAI_CONNECT_TIMEOUT_SECONDS` | 10초 |
| 응답 제한시간 | `OPENAI_READ_TIMEOUT_SECONDS` | 120초 |

식단·추천 시술·리포트는 JSON Schema의 엄격한 구조화 출력을 사용합니다. 챗봇은 텍스트 출력을 사용합니다. 모든 요청은 제공자 측 저장을 끈 `store=false`, 추론 강도 `low`로 호출하며 네트워크·5xx 장애만 1회 재시도합니다.

## 3. AI 공통 상태

| 상태 | 설명 |
| --- | --- |
| `PROCESSING` | 분석 진행 중 |
| `DRAFT` | 분석 완료, 사용자 확인 전 |
| `RECORDED` | 사용자가 기록하기를 눌러 일반 식단 저장 완료 |
| `FAILED` | 분석 실패, 직접 입력 가능 |
| `CANCELLED` | 사용자가 분석 초안 취소 |

AI가 음식을 하나도 인식하지 못하면 상태를 `FAILED`로 저장하고 `manualInputAvailable: true`를 반환하여 프론트가 직접 입력 화면으로 전환합니다. 사용자는 실패 상태에도 음식을 직접 추가해 DRAFT로 바꿀 수 있습니다. 식약처 데이터 API 연결은 아직 없으므로 현재 AI 결과를 `AI_ESTIMATE`로 명확히 표시하며, 식약처 출처를 가장하지 않습니다.

## 4. 현재 구현 선택

- 채팅·식단·추천·리포트 생성은 MVP에서 동기 응답입니다.
- 챗봇은 최근 20개 메시지와 최근 14일 개인화 요약을 사용합니다.
- 추천 식단은 대상 날짜까지 최근 14일 개인화 요약과 요청한 끼니 유형을 사용하며 음식은 최대 4개입니다.
- 추천 식단의 음식별 영양값 중 하나라도 null이면 해당 총 영양값도 null입니다.
- 시술 추천은 눈바디 이미지와 최근 30일 요약을 사용하고 최대 3개만 반환합니다.
- 리포트는 요청 기간 최대 31일의 날짜별 기록을 사용하고 요약·우선순위·방법을 각각 최대 3개 반환합니다.
- 이메일·닉네임·정확한 생년월일·자유 입력 메모는 AI에 보내지 않습니다. 생년월일과 출산일은 나이와 산후 경과일로 변환합니다.
- 운영 단계에서 응답 지연과 비용을 확인한 뒤 스트리밍·비동기 처리·rate limit을 추가합니다.

## 5. MVP 운영 정책

- 음식 분석 제한시간은 60초이며 일시적 네트워크·제공자 장애에 한해 1회 재시도합니다.
- 사용자별 일일 분석 횟수는 제한하지 않습니다. 운영 전 중복 클릭 방지와 rate limit을 추가합니다.
- confidence 숫자는 사용자에게 직접 표시하지 않습니다. 신뢰도가 낮은 항목에만 `확인이 필요해요` 안내를 표시합니다.
- 식단 분석에는 현재 사진 또는 음식 설명만 전달하며 사용자 프로필은 보내지 않습니다.
- 이름, 이메일, 전화번호, 주소, 정확한 생년월일, 출산일, 일기 원문, 감정·신체·피부 기록 전체는 식단 분석 모델에 전달하지 않습니다.
- 사용자가 식단 또는 계정을 삭제하면 1년 보관 정책보다 사용자 삭제 요청을 우선하여 연결된 사진도 삭제합니다.
- null인 kcal·영양소는 합계에서 제외하고, 응답에 영양정보가 불완전함을 알리는 별도 표시를 추가합니다.
