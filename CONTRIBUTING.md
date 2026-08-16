# 협업 가이드

## 브랜치와 커밋

- 기능: `feat/<domain>-<short-description>`
- 수정: `fix/<domain>-<short-description>`
- 문서: `docs/<short-description>`
- 커밋은 하나의 목적만 담고 `feat:`, `fix:`, `docs:`, `test:`, `refactor:` 형식을 사용합니다.

## Pull Request 최소 조건

1. 변경 목적과 영향을 받는 화면/API를 설명합니다.
2. Request/Response 변경 전후 예시를 첨부합니다.
3. DB 변경은 새 Flyway migration으로 추가합니다. 기존 migration은 수정하지 않습니다.
4. `./gradlew clean test bootJar`를 통과시킵니다.
5. 비밀값, 실제 `.env`, 개인정보가 포함되지 않았는지 확인합니다.

## API 계약 변경

JSON 키, 타입, null 허용 여부, enum, HTTP 상태가 바뀌면 다음 항목을 같은 PR에서 갱신합니다.

- Request/Response DTO와 Validation
- Swagger/OpenAPI
- 통합 테스트
- README JSON 계약
- MVP API 명세서
- 프론트 TypeScript 타입과 목업 데이터

프론트·기획 합의가 필요한 필드는 이슈 또는 회의 기록에 결론을 남긴 뒤 구현합니다.

## 현재 미확정 항목

- `skinCondition` 선택지
- `menstrualStatus`의 boolean/enum 여부
- `activityAmount`의 의미와 단위
- 식단 한 건이 음식 하나인지 여러 음식 묶음인지
- 권장 칼로리 개인화 산식
