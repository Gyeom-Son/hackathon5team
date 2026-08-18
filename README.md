# Moodprint Android

기존 인터랙티브 와이어프레임을 Kotlin과 Jetpack Compose로 옮긴 Android MVP입니다.

## 개발 환경

- Android Studio Quail 이상
- Android SDK 37
- JDK 17 이상(Android Studio 내장 JDK 권장)

앱의 최소 지원 버전은 Android 8(API 26)입니다. 기존 iOS 구현은 저장소에
참고용으로 보존되어 있습니다.

## 처음 실행하기

Android Studio에서 Android 프로젝트를 엽니다.

```bash
open -a "Android Studio" MoodprintAndroid
```

Gradle 동기화가 끝나면 Android 가상 기기 또는 연결한 Android 기기를 선택하고
Run(`⌃R`)을 누릅니다.

## 명령줄 빌드와 테스트

Debug APK 빌드:

```bash
cd MoodprintAndroid
./gradlew :app:assembleDebug
```

APK는 `MoodprintAndroid/app/build/outputs/apk/debug/app-debug.apk`에 생성됩니다.

테스트:

```bash
./gradlew test
```

Room 마이그레이션과 동기화 outbox 계측 테스트는 실행 중인 에뮬레이터에서
`./gradlew connectedDebugAndroidTest`로 확인합니다.

## Release API 설정

Release 동기화를 켜려면 빌드 시 운영 API URL을 Gradle property로 전달합니다.

```bash
./gradlew :app:assembleRelease -PMOODPRINT_API_BASE_URL=https://example.com/api/v1
```

해당 property가 없으면 Release APK는 정상 빌드되지만 원격 동기화만 비활성화됩니다.
로컬 Room 저장과 대기 작업은 유지됩니다.

## 로그인 없는 백엔드 실행

Moodprint는 로그인 화면 없이 서버가 자동 발급하는 익명 토큰을 사용합니다. Android는
기기 Room DB에 먼저 저장하므로 백엔드가 꺼져 있어도 핵심 흐름을 계속 사용할 수 있습니다.

```bash
cd MoodprintBackend
./gradlew bootRun
```

- API: `http://localhost:8080/api/v1`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Android Emulator에서는 `http://10.0.2.2:8080/api/v1`으로 연결합니다.
- 서버 테스트: `cd MoodprintBackend && ./gradlew test`

서버 구성과 API 계약은 `MoodprintBackend/README.md`를 참고하세요.

## 포함된 흐름

시작 → 캐릭터 소개 → 감정과 텍스트 기록 → 개인화 행동 추천 → 행동 실행 →
변화 기록(선택 사항) → 펫 성장 → 도감

기록을 건너뛰거나 행동 후 기분이 나아지지 않아도 불이익이 없으며,
행동 완료 자체가 펫 성장으로 이어집니다.

## 팀 데모 시나리오

1. 첫 실행에서 시작하기를 누르고 Moodprint 캐릭터 소개를 확인합니다.
2. 감정을 1~3개 선택하고 에너지와 선택적 메모를 기록합니다.
3. 입력과 이전 행동 결과를 바탕으로 표시된 행동 추천 이유를 확인합니다.
4. 추천 행동을 실행하고 타이머의 일시정지와 재개를 확인합니다.
5. 행동 후 변화를 선택하거나 건너뜁니다.
6. 변화 결과와 관계없이 펫 경험치 또는 도감 진행도가 증가하는지 확인합니다.
7. 기록 탭과 도감 탭에서 저장된 결과를 확인한 뒤 앱을 재실행해 유지 여부를
   확인합니다.
