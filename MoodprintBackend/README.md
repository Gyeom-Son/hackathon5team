# Moodprint Backend

Android MVP는 Room 로컬 데이터를 원본으로 사용하고, 이 서버는 익명 토큰에 연결된 선택적 사본 저장소로 동작합니다. 로그인과 기기 간 복원은 현재 지원하지 않으며, 앱을 재설치하면 기존 서버 사본을 다시 연결할 수 없습니다. 추천 순위는 Android의 로컬 규칙이 데모 기준이며 서버 추천 API는 향후 원격 규칙 전환을 위한 호환 API입니다.

Moodprint iOS/Android MVP용 Spring Boot + Kotlin API입니다. 로그인 대신 서버가 발급한 opaque 익명 토큰을 사용하며, 서버는 SHA-256 hash만 저장합니다.

## 실행

```bash
./gradlew bootRun
```

- 로컬 H2: `jdbc:h2:file:./data/moodprint`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Android emulator base URL: `http://10.0.2.2:8080`
- PostgreSQL: `SPRING_PROFILES_ACTIVE=postgres` 및 `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`
- 상태 확인: `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness`
- 배포 서버가 주입하는 포트: `PORT` (기본 8080)

PostgreSQL 프로필은 Flyway migration을 적용한 뒤 Hibernate schema validation을 수행합니다.
익명 세션 생성 제한은 `MOODPRINT_SESSION_RATE_LIMIT_REQUESTS`(기본 20)와
`MOODPRINT_SESSION_RATE_LIMIT_WINDOW_SECONDS`(기본 60)로 조정할 수 있습니다. 운영에서는
reverse proxy의 rate limit도 함께 사용하세요.
  (Flyway가 `db/migration`의 초기 스키마를 자동 적용합니다.)

### Docker 실행 확인

```bash
docker build -t moodprint-backend .
docker run --rm -p 8080:8080 moodprint-backend
```

위 명령은 로컬 H2 실행 확인용이며 컨테이너를 제거하면 내부 데이터도 사라집니다.
운영 컨테이너에는 반드시 `SPRING_PROFILES_ACTIVE=postgres`와 PostgreSQL 환경변수를
주입해, 프로필 누락으로 H2를 사용하는 일이 없도록 합니다.

운영에서는 `.env.example`의 환경변수를 배포 서비스의 Secret/Environment 설정에
등록합니다. `.env` 파일이나 DB 비밀번호는 Git에 올리지 않습니다. `DATABASE_URL`은
반드시 `jdbc:postgresql://...` 형식이어야 하며 외부 무료 DB를 사용할 때는 일반적으로
`?sslmode=require`를 붙입니다.

## 인증

`POST /api/v1/anonymous-sessions`로 토큰을 받고 다음 헤더를 사용합니다.

```text
Authorization: Bearer <token>
```

## API

- `POST /api/v1/anonymous-sessions`
- `POST /api/v1/moods` — `clientMoodId` 옵션. 같은 소유자와 ID로 재시도하면 기존 응답 반환
- `GET /api/v1/moods?page=0&size=100` — `size` 최대 500
- `GET /api/v1/actions`
- `POST /api/v1/recommendations`
- `POST /api/v1/action-completions` — `sessionId`로 멱등성 보장
- `GET /api/v1/pets`
- `GET /api/v1/records/monthly?year=2026&month=8`
- `DELETE /api/v1/me` — 현재 토큰에 연결된 기록, 행동 결과, 보상과 펫 진행도를 모두 삭제

`change`는 선택이며 `null`이거나 어떤 값이든 행동 완료 보상은 동일합니다. 추천은 사용자가 직접 선택한 감정·에너지와 비슷한 과거 행동 결과만 사용하며 텍스트를 자동 분석하지 않습니다.

## 개인정보와 운영

감정, 선택 메모와 행동 결과는 익명 토큰에 연결된 민감 데이터로 취급합니다. 로그에는 요청
본문이나 메모 원문을 남기지 않습니다. 모든 응답에는 추적용 `X-Request-ID`가 포함되며,
클라이언트가 안전한 형식의 ID를 보내면 동일한 값을 사용합니다. 사용자가 삭제를 요청하면
`DELETE /api/v1/me`로 연결 데이터를 즉시 삭제합니다. 현재 MVP에는 자동 보존 만료가 없으므로
외부 운영 전 조직의 보존 기간을 정하고 만료 작업과 백업 삭제 정책을 추가해야 합니다.

운영 프로필에서는 Swagger를 기본 비활성화합니다. 임시 확인이 필요할 때만
`SWAGGER_ENABLED=true`를 설정하세요. Android 네이티브 앱은 CORS 설정이 필요하지
않습니다. 별도의 웹 프론트를 연결할 경우에만 `MOODPRINT_CORS_ALLOWED_ORIGINS`에
허용할 HTTPS origin을 쉼표로 구분해 입력합니다.

서버의 기록 날짜 판정 기준은 기본적으로 `Asia/Seoul`이며
`MOODPRINT_TIME_ZONE`으로 변경할 수 있습니다. API 응답에는 `Cache-Control: no-store`,
`X-Content-Type-Options: nosniff`, `X-Request-ID`가 포함됩니다.

## 테스트

```bash
./gradlew test
```

무료 배포 절차와 배포 후 점검 항목은 [DEPLOYMENT.md](DEPLOYMENT.md)를 참고합니다.
