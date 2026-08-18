# Moodprint Agent Rules

## Project

Moodprint는 감정과 행동 변화를 기록하고, 작은 회복 행동을 실행하며,
펫을 성장시키고 도감을 수집하는 Android 앱이다. 현재 주 개발 대상은
`MoodprintAndroid`이며 기존 iOS 구현은 참고용으로 보존한다.

핵심 흐름:

`시작하기 → 캐릭터 소개 → 감정 선택과 텍스트 기록 → 개인화 행동 추천
→ 행동 실행 → 행동 변화 기록 → 펫 성장 또는 도감 획득`

## Required Product Rules

- Android 네이티브 앱은 Kotlin과 Jetpack Compose를 기본으로 한다.
- Material 3, 시스템 글꼴, 시스템 인셋과 48dp 이상 터치 영역을 따른다.
- AI 챗봇, 음성 입력, 자동 감정 분석, 대화형 상담, 커뮤니티를 추가하지 않는다.
- 사용자가 입력한 감정과 에너지, 기존 행동 결과만 개인화 근거로 사용한다.
- 기분이 나아지지 않아도 행동을 완료하면 펫은 성장한다.
- 변화 기록은 선택 사항이며, 기록하지 않은 사용자에게 불이익을 주지 않는다.
- MVP 범위는 펫 성장과 펫 도감 수집이다.
- 기록, 행동, 행동 결과, 보상 데이터를 분리해 향후 마음 레시피로 확장 가능하게 유지한다.

## Source of Truth

우선순위는 다음과 같다.

1. 사용자의 최신 요청
2. 이 파일의 공통 규칙
3. 확정된 작업 계획
4. `docs/moodprint-wireframe.html`
5. 기존 SwiftUI 구현

충돌하거나 불확실한 내용은 추측으로 확정하지 않는다.

## Working Rules

- 요청과 직접 관련된 범위만 변경한다.
- Android 변경은 `MoodprintAndroid` 안에서 수행하며 Compose 구조를 우선한다.
- 큰 리팩터링이나 외부 패키지 추가는 근거와 필요성이 있을 때만 한다.
- 구현 후 최소한 `./gradlew :app:assembleDebug` 빌드를 수행한다.
- UI 변경은 가능한 경우 Android Emulator에서 실제 화면을 확인한다.
- 사용자 데이터나 건강 관련 표현을 진단·치료·상담처럼 표현하지 않는다.

## Xcode 프로젝트 관리 정책

- `project.yml`을 Xcode 프로젝트 구성의 단일 원본으로 사용한다.
- `*.xcodeproj/project.pbxproj`, `*.xcodeproj/xcshareddata` 내부 생성 파일과
  XcodeGen이 생성한 프로젝트 파일을 직접 수정하지 않는다.
- Target 추가·제거, Source·Resource 경로, Swift Package, Build Setting,
  Scheme, Info.plist와 Entitlements 연결은 반드시 `project.yml`에서 변경한다.
- 새로운 Swift 파일은 `project.yml`에 선언된 기능별 소스 디렉터리에 생성하며,
  Xcode GUI에서 수동으로 등록하지 않는다.
- 프로젝트 구성 변경 후 프로젝트 루트에서 `xcodegen generate`를 실행한다.
- 생성 후 다음 명령으로 iOS 시뮬레이터 빌드를 검증한다.

```bash
xcodebuild \
  -project Moodprint.xcodeproj \
  -scheme Moodprint \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  CODE_SIGNING_ALLOWED=NO \
  build
```

- 빌드 실패 시 `project.pbxproj`를 고치지 않고 `project.yml`, 빌드 설정,
  테스트 설정 또는 Swift 소스에서 원인을 수정한 뒤 프로젝트를 재생성한다.
- `project.yml`을 변경한 상태에서 `xcodegen generate`와 빌드 검증 없이
  작업 완료를 선언하지 않는다.

## Agent Workflow

작업 성격에 맞는 문서를 `docs/agents`에서 선택해 따른다. 복잡한 기능은 기본적으로:

1. Planner
2. Plan Critic
3. Overall Reviewer
4. Code Writer
5. Code Critic
6. Criticism Judge
7. Plan Alignment Reviewer
8. Agent Rule Reviewer

순서로 검토한다. 단순 수정은 필요한 역할만 사용한다.
