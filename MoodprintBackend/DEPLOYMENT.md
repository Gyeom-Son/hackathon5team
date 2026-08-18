# 무료 배포 준비 가이드

실제 배포는 Android 화면과 API 주소가 확정된 뒤 진행합니다. 현재 권장 조합은
`Render 무료 Web Service + Neon 무료 PostgreSQL`입니다. Render의 무료 PostgreSQL은
일정 기간 뒤 만료되므로 장기 데모 데이터 저장소로 사용하지 않습니다.

## 1. Neon PostgreSQL 준비

1. Neon에서 무료 프로젝트와 데이터베이스를 생성합니다.
2. 연결 정보에서 host, database, user, password를 확인합니다.
3. 배포 서비스에는 다음 형식으로 각각 등록합니다.

```text
DATABASE_URL=jdbc:postgresql://HOST/DATABASE?sslmode=require
DATABASE_USERNAME=USER
DATABASE_PASSWORD=PASSWORD
```

Neon이 제공한 `postgresql://USER:PASSWORD@HOST/DATABASE?...` 문자열을 그대로
`DATABASE_URL`에 넣지 않습니다. Java JDBC 형식으로 바꾸고 사용자명과 비밀번호는
별도 환경변수로 분리합니다.

## 2. Render Web Service 준비

1. GitHub 저장소를 Render에 연결하고 새 Web Service를 만듭니다.
2. Root Directory를 `MoodprintBackend`, Runtime을 Docker로 선택합니다.
3. 무료 instance를 선택하고 health check path를
   `/actuator/health/readiness`로 지정합니다.
4. 다음 환경변수를 등록합니다.

```text
SPRING_PROFILES_ACTIVE=postgres
DATABASE_URL=jdbc:postgresql://HOST/DATABASE?sslmode=require
DATABASE_USERNAME=USER
DATABASE_PASSWORD=PASSWORD
DATABASE_MAX_POOL_SIZE=5
DATABASE_MIN_IDLE=0
MOODPRINT_TIME_ZONE=Asia/Seoul
SWAGGER_ENABLED=false
```

`PORT`는 Render가 주입하므로 직접 만들 필요가 없습니다. Android 앱만 연결한다면
CORS 설정도 필요하지 않습니다.

## 3. 배포 직후 확인

```bash
curl -fsS https://YOUR-SERVICE.onrender.com/actuator/health/readiness
curl -fsS https://YOUR-SERVICE.onrender.com/api/v1/actions
curl -fsS -X POST https://YOUR-SERVICE.onrender.com/api/v1/anonymous-sessions
```

health 응답이 `UP`인지 확인하고, 재배포 후에도 앞서 생성한 익명 사용자의 데이터가
Neon에 유지되는지 확인합니다. 로그에는 감정 메모나 Bearer token 원문이 없어야 합니다.

## 4. Android release 연결

서버 주소의 마지막에 반드시 `/api/v1`을 포함합니다.

```bash
cd MoodprintAndroid
./gradlew assembleRelease \
  -PMOODPRINT_API_BASE_URL=https://YOUR-SERVICE.onrender.com/api/v1
```

무료 Web Service는 일정 시간 요청이 없으면 sleep 상태가 되어 첫 요청이 느릴 수
있습니다. 데모 시작 전에 health endpoint를 한 번 열어 깨우는 것이 안전합니다.

## 대안

- Koyeb 무료 Web Service + Neon: Render보다 sleep 진입 시간이 길지만 무료 instance의
  CPU가 작고 지역 선택이 제한적입니다.
- Google Cloud Run + Neon: 서울 region과 넉넉한 무료 사용량이 장점이지만 결제 계정이
  필요하고 무료 한도를 넘으면 비용이 발생할 수 있어 예산 알림과 최대 instance 제한이
  필수입니다.

무료 요금제는 데모와 해커톤용입니다. 실제 사용자 데이터를 장기간 운영하기 전에는
유료 전환, 백업, 보존 기간, 장애 알림과 개인정보 처리방침을 확정합니다.
