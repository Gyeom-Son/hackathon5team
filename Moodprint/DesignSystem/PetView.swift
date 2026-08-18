import SwiftUI
import UIKit

enum PetMood {
    case calm
    case happy
}

/// 16종 동물(고양이·강아지·토끼·곰·여우·판다·사자·호랑이·코알라·다람쥐·펭귄·부엉이·양·돼지·사슴·병아리)을
/// 성장 3단계(아기 → 성장 → 완성형)로 그려주는 펫 캐릭터 뷰입니다.
///
/// 좌표는 `mongsili_animal_growth_stages_2.html` 참고자료의 `.stage`(90x96) / `.body`(80x74, 몸통 위쪽에
/// 14px 여백)를 그대로 비율로 옮긴 것입니다. 몸통이 프레임 전체를 채우지 않고 위쪽에 실제 빈 공간
/// (headroom)을 남겨야 귀·꼬리 같은 장식이 몸통 실루엣에 묻히지 않고 또렷하게 보입니다.
struct PetView: View {
    let size: CGFloat
    let mood: PetMood
    var animal: AnimalKind = .cat
    var stage: Int = 3
    var name: String = ""
    var unlocked: Bool = true

    private var clampedStage: Int { min(max(stage, 1), 3) }

    private var bodyScale: CGFloat {
        switch clampedStage {
        case 1: 0.62
        case 2: 0.84
        default: 1.0
        }
    }

    private var featureScale: CGFloat {
        switch clampedStage {
        case 1: 0.76
        case 2: 0.88
        default: 1.0
        }
    }

    private var faceScale: CGFloat {
        switch clampedStage {
        case 1: 1.12
        case 2: 1.05
        default: 1.0
        }
    }

    private var saturation: Double {
        switch clampedStage {
        case 1: 0.68
        case 2: 0.88
        default: 1.05
        }
    }

    /// 몸통 하단 중앙 기준으로 커진다 — 몸통을 더 둥글게 늘리면서 바닥선이 .96 지점으로 내려간 것에 맞췄다.
    private var bodyAnchor: UnitPoint { UnitPoint(x: 0.5, y: 0.96) }
    /// 참고 HTML의 transform-origin: 50% 12% (몸통 위쪽 장식 기준점).
    private var featureAnchor: UnitPoint { UnitPoint(x: 0.5, y: 0.24) }

    private var displayName: String { name.isEmpty ? animal.koreanName : name }
    private var bodyColor: Color { unlocked ? animal.bodyColor : Color(hex: 0xB9B5C4) }
    /// 귀처럼 몸통과 맞닿는 장식은 몸통과 완전히 같은 색이면 실루엣에 묻혀 안 보이므로
    /// 살짝 더 어둡게 만들어 항상 구분되게 한다.
    private var earColor: Color { bodyColor.darker(by: 0.1) }

    var body: some View {
        ZStack {
            Ellipse()
                .fill(Color.black.opacity(0.08))
                .frame(width: size * 0.58, height: size * 0.05)
                .offset(y: size * 0.485)
                .accessibilityHidden(true)

            behindBodyDecorations
                .frame(width: size, height: size)
                .scaleEffect(bodyScale, anchor: bodyAnchor)

            MoodprintBlobShape()
                .fill(bodyColor)
                .saturation(saturation)
                .frame(width: size, height: size)
                .overlay(frontBodyDecorations)
                .overlay(cheeks)
                .overlay(faceContent.scaleEffect(faceScale))
                .scaleEffect(bodyScale, anchor: bodyAnchor)
        }
        .grayscale(unlocked ? 0 : 1)
        .opacity(unlocked ? 1 : 0.45)
        .frame(width: size, height: size)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text(unlocked ? "마음 동반자 \(displayName)" : "아직 만나지 못한 마음 생물"))
        .accessibilityValue(unlocked ? (mood == .happy ? Text("기쁜 표정, 성장 \(clampedStage)단계") : Text("편안한 표정, 성장 \(clampedStage)단계")) : Text(""))
    }

    // MARK: - Decoration layers

    /// 갈기·꼬리처럼 몸통 뒤에 깔리는 장식.
    @ViewBuilder
    private var behindBodyDecorations: some View {
        ZStack {
            if animal == .lion {
                LionMane(color: animal.bodyColor.darker(by: 0.14), size: size)
            }
            if [.cat, .dog, .fox, .lion, .pig].contains(animal) {
                tail
            }
            if animal == .squirrel {
                squirrelTail
            }
        }
        .scaleEffect(featureScale, anchor: featureAnchor)
    }

    /// 귀·수염·줄무늬 등 몸통 앞에 얹히는 장식.
    @ViewBuilder
    private var frontBodyDecorations: some View {
        ZStack {
            // 반짝임을 귀보다 먼저(아래에) 그려서, 귀와 겹치는 자리에서는 귀 뒤로 자연스럽게
            // 가려지게 한다 (이전에는 맨 위에 그려서 귀를 가로지르는 것처럼 보였다).
            if clampedStage == 3 && unlocked {
                sparkle
            }
            ears
            if animal == .cat { whiskers }
            if animal == .tiger { stripes }
            if [.tiger, .fox].contains(animal) { muzzlePatch }
            if animal == .bear { bearMuzzle }
            if animal == .panda { eyePatches }
            if animal == .sheep { wool }
            if animal == .deer { antlers }
            if animal == .squirrel { cheekPuffs }
            if animal == .penguin { belly }
            if [.penguin, .owl, .chick].contains(animal) { wings }
            if animal == .chick { tuft }
            if animal == .pig { snout }
            if [.penguin, .owl, .chick].contains(animal) { beak }
        }
        .scaleEffect(featureScale, anchor: featureAnchor)
    }

    private var sparkle: some View {
        Text("✦")
            .font(.system(size: size * 0.14))
            .foregroundStyle(Color(hex: 0xF0A94E))
            .offset(x: size * 0.34, y: -size * 0.34)
    }

    // MARK: - Ears
    // 아래 오프셋들은 모두 `mongsili_animal_growth_stages_2.html`의 .stage(90x96) 좌표를
    // size 기준 비율로 그대로 옮긴 값이다 (offset = (targetFraction - 0.5) * size).

    @ViewBuilder
    private var ears: some View {
        switch animal {
        case .cat:
            HStack(spacing: size * 0.24) {
                TriangleShape().fill(earColor)
                    .frame(width: size * 0.22, height: size * 0.22)
                    .rotationEffect(.degrees(-14))
                TriangleShape().fill(earColor)
                    .frame(width: size * 0.22, height: size * 0.22)
                    .rotationEffect(.degrees(14))
            }
            .offset(y: -size * 0.385)
        case .fox:
            // 고양이보다 더 길고 뾰족한 귀 + 짙은 귀 끝으로 여우임을 구분한다.
            // 귀 몸통과 귀 끝 무늬를 각각 따로 회전시켰더니 서로 다른 축을 기준으로 돌아가서
            // 회전 각도만큼 어긋나 보였다 — 두 도형을 한 그룹으로 묶어 같은 축으로 함께
            // 회전시켜 정확히 맞춘다.
            ZStack {
                ForEach([-1.0, 1.0], id: \.self) { sign in
                    ZStack {
                        TriangleShape().fill(earColor)
                            .frame(width: size * 0.22, height: size * 0.28)
                        TriangleShape().fill(Color(hex: 0x2C2C34).opacity(0.55))
                            .frame(width: size * 0.11, height: size * 0.11)
                            .offset(y: -size * 0.10)
                    }
                    .rotationEffect(.degrees(sign * 12))
                    .offset(x: sign * size * 0.24, y: -size * 0.43)
                }
            }
        case .dog:
            // 자연스러운 처진 귀: 곧은 알약 모양 대신 통통한 타원으로 정수리 옆에 붙여
            // 뺨 옆으로 늘어지게 하고, 안쪽에 옅은 분홍을 더해 귀처럼 보이게 한다.
            // 중심에서 0.40만큼 벌어져 있으면 몸통 실루엣 밖으로 반쯤 튀어나가 붕 떠 보인다
            // (심지어 프레임 밖으로도 잘렸다) — 중심으로 더 당기고 위로 올려 머리에 붙게 한다.
            ZStack {
                ForEach([-1.0, 1.0], id: \.self) { sign in
                    ZStack {
                        Ellipse().fill(earColor)
                            .frame(width: size * 0.26, height: size * 0.42)
                        Ellipse().fill(Color(hex: 0xFFD7E0).opacity(0.55))
                            .frame(width: size * 0.14, height: size * 0.22)
                            .offset(y: size * 0.05)
                    }
                    .rotationEffect(.degrees(sign * 14))
                    .offset(x: sign * size * 0.32, y: -size * 0.26)
                }
            }
        case .rabbit:
            // 귀가 몸통에서 붕 떠 보이지 않을 만큼만 겹치게 하되(-0.36은 너무 내려와 얼굴에
            // 파묻혀 보였다), 토끼 특유의 길게 솟은 귀 느낌은 유지한다.
            HStack(spacing: size * 0.22) {
                Capsule().fill(earColor)
                    .frame(width: size * 0.16, height: size * 0.46)
                    .rotationEffect(.degrees(-8))
                Capsule().fill(earColor)
                    .frame(width: size * 0.16, height: size * 0.46)
                    .rotationEffect(.degrees(8))
            }
            .offset(y: -size * 0.52)
        case .bear:
            roundEarPair(diameter: size * 0.29, dot: true, spreadX: 0.28, offsetY: -size * 0.41)
        case .panda:
            roundEarPair(diameter: size * 0.29, color: Color(hex: 0x2C2C34), dot: false, spreadX: 0.28, offsetY: -size * 0.41)
        case .lion:
            roundEarPair(diameter: size * 0.22, dot: true, spreadX: 0.311, offsetY: -size * 0.375)
        case .tiger:
            roundEarPair(diameter: size * 0.20, dot: true, spreadX: 0.322, offsetY: -size * 0.385)
        case .koala:
            roundEarPair(diameter: size * 0.33, dot: true, dotScale: 0.5, spreadX: 0.40, offsetY: -size * 0.344)
        case .squirrel:
            roundEarPair(diameter: size * 0.156, dot: false, spreadX: 0.30, offsetY: -size * 0.365)
        case .owl:
            triangleEarPair(width: size * 0.11, height: size * 0.10, color: earColor, spacing: size * 0.178, offsetY: -size * 0.396)
        case .sheep:
            triangleEarPair(width: size * 0.156, height: size * 0.135, color: Color(hex: 0xC9A27A), spacing: size * 0.31, offsetY: -size * 0.266)
        case .pig:
            triangleEarPair(width: size * 0.156, height: size * 0.135, color: earColor, spacing: size * 0.31, offsetY: -size * 0.391)
        case .deer:
            triangleEarPair(width: size * 0.156, height: size * 0.135, color: earColor, spacing: size * 0.31, offsetY: -size * 0.328)
        case .penguin, .chick:
            EmptyView()
        }
    }

    private func roundEarPair(
        diameter: CGFloat,
        color: Color? = nil,
        dot: Bool,
        dotScale: CGFloat = 0.42,
        spreadX: CGFloat,
        offsetY: CGFloat
    ) -> some View {
        HStack(spacing: max(2 * (0.5 - spreadX) * size - diameter, 0)) {
            ForEach(0..<2, id: \.self) { _ in
                ZStack {
                    Circle().fill(color ?? earColor).frame(width: diameter, height: diameter)
                    if dot {
                        Circle().fill(Color(hex: 0xFFD7E0)).frame(width: diameter * dotScale, height: diameter * dotScale)
                    }
                }
            }
        }
        .offset(y: offsetY)
    }

    private func triangleEarPair(width: CGFloat, height: CGFloat, color: Color, spacing: CGFloat, offsetY: CGFloat) -> some View {
        HStack(spacing: spacing) {
            TriangleShape().fill(color).frame(width: width, height: height).rotationEffect(.degrees(-10))
            TriangleShape().fill(color).frame(width: width, height: height).rotationEffect(.degrees(10))
        }
        .offset(y: offsetY)
    }

    // MARK: - Other features

    private var tail: some View {
        UnevenRoundedRectangle(
            topLeadingRadius: 0,
            bottomLeadingRadius: size * 0.12,
            bottomTrailingRadius: size * 0.12,
            topTrailingRadius: size * 0.12
        )
        .fill(animal == .fox ? Color.white.opacity(0.9) : earColor)
        .frame(width: animal == .pig ? size * 0.13 : size * 0.24, height: animal == .pig ? size * 0.13 : size * 0.24)
        .rotationEffect(.degrees(30))
        .offset(x: animal == .pig ? size * 0.48 : size * 0.43, y: animal == .pig ? size * 0.16 : size * 0.24)
    }

    private var squirrelTail: some View {
        UnevenRoundedRectangle(
            topLeadingRadius: size * 0.03,
            bottomLeadingRadius: size * 0.15,
            bottomTrailingRadius: size * 0.15,
            topTrailingRadius: size * 0.15
        )
        .fill(earColor)
        .frame(width: size * 0.3, height: size * 0.38)
        .rotationEffect(.degrees(10))
        .offset(x: size * 0.4, y: size * 0.09)
    }

    private var whiskers: some View {
        // 연보라 선, 그다음 흰색 선 모두 고양이 몸통(밝은 살구색)과 대비가 약해 거의 안 보였다.
        // 진한 잉크색으로 바꿔 배경색과 무관하게 뚜렷이 보이게 한다.
        ZStack {
            ForEach([-1.0, 1.0], id: \.self) { sign in
                VStack(spacing: size * 0.06) {
                    Rectangle().fill(Color(hex: 0x3A3A44).opacity(0.9)).frame(width: size * 0.2, height: 1.6)
                        .rotationEffect(.degrees(sign * -5))
                    Rectangle().fill(Color(hex: 0x3A3A44).opacity(0.9)).frame(width: size * 0.21, height: 1.6)
                    Rectangle().fill(Color(hex: 0x3A3A44).opacity(0.9)).frame(width: size * 0.2, height: 1.6)
                        .rotationEffect(.degrees(sign * 5))
                }
                .offset(x: sign * size * 0.44, y: size * 0.08)
            }
        }
    }

    /// 여우·호랑이 특유의 밝은 턱·볼 무늬. 눈·코보다 먼저 그려서 그 위에 이목구비가 얹히게 한다.
    private var muzzlePatch: some View {
        Ellipse()
            .fill(Color(hex: 0xFFF8EF).opacity(0.92))
            .frame(width: size * 0.34, height: size * 0.26)
            .offset(y: size * 0.09)
    }

    /// 곰 특유의 밝은 주둥이: 눈 아래~입 주변을 밝은 색 타원으로 감싸 몸통과 구분되는 얼굴 부위를 만든다.
    private var bearMuzzle: some View {
        Ellipse()
            .fill(Color(hex: 0xF3E4C8).opacity(0.85))
            .frame(width: size * 0.28, height: size * 0.22)
            .offset(y: size * 0.10)
    }

    private var stripes: some View {
        // 무늬 하나하나의 세로 길이(0.16)가 줄 사이 간격(0.04~0.07)보다 훨씬 커서
        // 서로 겹쳐 뭉쳐 보였다. 길이를 짧게 줄이고 줄 사이 간격을 넉넉히 벌려 겹치지 않게 한다.
        // 정수리 세로 무늬 1개 + 이마 대각선 2쌍.
        let specs: [(x: CGFloat, y: CGFloat, angle: Double)] = [
            (0.5, 0.22, 0),
            (0.36, 0.27, 14),
            (0.64, 0.27, -14),
            (0.30, 0.34, 24),
            (0.70, 0.34, -24),
        ]
        return ZStack {
            ForEach(0..<specs.count, id: \.self) { i in
                let spec = specs[i]
                Capsule().fill(Color(hex: 0x2C2C34)).opacity(0.88)
                    .frame(width: size * 0.035, height: size * 0.09)
                    .rotationEffect(.degrees(spec.angle))
                    .offset(x: (spec.x - 0.5) * size, y: (spec.y - 0.5) * size)
            }
        }
    }

    private var eyePatches: some View {
        HStack(spacing: size * 0.18) {
            Ellipse().fill(Color(hex: 0x2C2C34)).opacity(0.85)
                .frame(width: size * 0.2, height: size * 0.16)
                .rotationEffect(.degrees(-15))
            Ellipse().fill(Color(hex: 0x2C2C34)).opacity(0.85)
                .frame(width: size * 0.2, height: size * 0.16)
                .rotationEffect(.degrees(15))
        }
        .offset(y: -size * 0.10)
    }

    private var wool: some View {
        ZStack {
            ForEach(0..<4, id: \.self) { i in
                Circle().fill(Color(hex: 0xF7F4EC))
                    .frame(width: size * 0.29, height: size * 0.29)
                    .offset(
                        x: [-0.235, -0.078, 0.078, 0.235][i] * size,
                        y: [-0.365, -0.333, -0.333, -0.365][i] * size
                    )
            }
        }
    }

    private var antlers: some View {
        HStack(spacing: size * 0.26) {
            AntlerShape().stroke(Color(hex: 0xA9835A), lineWidth: size * 0.022)
                .frame(width: size * 0.14, height: size * 0.27)
                .rotationEffect(.degrees(-14))
            AntlerShape().stroke(Color(hex: 0xA9835A), lineWidth: size * 0.022)
                .frame(width: size * 0.14, height: size * 0.27)
                .scaleEffect(x: -1, y: 1)
                .rotationEffect(.degrees(14))
        }
        .offset(y: -size * 0.495)
    }

    private var cheekPuffs: some View {
        HStack(spacing: size * 0.55) {
            Circle().fill(Color.white.opacity(0.4)).frame(width: size * 0.17, height: size * 0.14)
            Circle().fill(Color.white.opacity(0.4)).frame(width: size * 0.17, height: size * 0.14)
        }
        .offset(y: size * 0.08)
    }

    private var belly: some View {
        Ellipse().fill(Color.white.opacity(0.95))
            .frame(width: size * 0.52, height: size * 0.32)
            .offset(y: size * 0.24)
    }

    private var wings: some View {
        HStack(spacing: size * 0.78) {
            Ellipse().fill(bodyColor.darker(by: 0.08))
                .frame(width: size * 0.16, height: size * 0.28)
                .rotationEffect(.degrees(-22))
            Ellipse().fill(bodyColor.darker(by: 0.08))
                .frame(width: size * 0.16, height: size * 0.28)
                .rotationEffect(.degrees(22))
        }
        .offset(y: size * 0.14)
    }

    private var tuft: some View {
        Capsule().fill(earColor)
            .frame(width: size * 0.1, height: size * 0.16)
            .rotationEffect(.degrees(-8))
            .offset(y: -size * 0.435)
    }

    private var snout: some View {
        ZStack {
            Ellipse().fill(Color(hex: 0xEF9FAE)).frame(width: size * 0.18, height: size * 0.12)
            HStack(spacing: size * 0.04) {
                Circle().fill(Color(hex: 0xC96B83)).frame(width: size * 0.03, height: size * 0.04)
                Circle().fill(Color(hex: 0xC96B83)).frame(width: size * 0.03, height: size * 0.04)
            }
        }
        .offset(y: size * 0.03)
    }

    private var beak: some View {
        TriangleShape()
            .fill(Color(hex: 0xF0A94E))
            .frame(width: size * 0.08, height: size * 0.07)
            .rotationEffect(.degrees(180))
            .offset(y: size * 0.09)
    }

    // MARK: - Face

    private var cheeks: some View {
        HStack {
            Ellipse().fill(Color.white.opacity(0.55)).frame(width: size * 0.08, height: size * 0.05)
            Spacer()
            Ellipse().fill(Color.white.opacity(0.55)).frame(width: size * 0.08, height: size * 0.05)
        }
        .padding(.horizontal, size * 0.16)
        .offset(y: -size * 0.02)
    }

    @ViewBuilder
    private var faceContent: some View {
        VStack(spacing: size * 0.045) {
            eyesView
            noseAndMouth
        }
        .offset(y: -size * 0.02)
    }

    @ViewBuilder
    private var eyesView: some View {
        switch animal {
        case .owl:
            HStack(spacing: size * 0.04) {
                ForEach(0..<2, id: \.self) { _ in
                    ZStack {
                        Circle().fill(Color.white).frame(width: size * 0.18, height: size * 0.18)
                        Circle().fill(MoodprintTheme.ink).frame(width: size * 0.09, height: size * 0.09)
                    }
                }
            }
        case .tiger, .fox:
            // 얇은 실선 하나였던 이전 버전은 감은 눈처럼 보였다. 아몬드 모양을 두껍게 키우고
            // 회전을 줄여서 뜬 눈처럼 보이게 하고, 작은 하이라이트로 생기를 더한다.
            HStack(spacing: size * 0.16) {
                ForEach([-1.0, 1.0], id: \.self) { sign in
                    ZStack {
                        Ellipse().fill(MoodprintTheme.ink).frame(width: size * 0.09, height: size * 0.09)
                            .rotationEffect(.degrees(sign * 10))
                        Circle().fill(Color.white).frame(width: size * 0.024, height: size * 0.024)
                            .offset(x: sign * size * 0.015, y: -size * 0.02)
                    }
                }
            }
        case .koala:
            HStack(spacing: size * 0.13) {
                Capsule().fill(MoodprintTheme.ink).frame(width: size * 0.09, height: size * 0.025)
                Capsule().fill(MoodprintTheme.ink).frame(width: size * 0.09, height: size * 0.025)
            }
        case .deer:
            HStack(spacing: size * 0.15) {
                Ellipse().fill(MoodprintTheme.ink).frame(width: size * 0.09, height: size * 0.1)
                Ellipse().fill(MoodprintTheme.ink).frame(width: size * 0.09, height: size * 0.1)
            }
        case .bear, .panda, .lion, .sheep, .chick:
            HStack(spacing: size * 0.11) {
                Circle().fill(MoodprintTheme.ink).frame(width: size * 0.09, height: size * 0.09)
                Circle().fill(MoodprintTheme.ink).frame(width: size * 0.09, height: size * 0.09)
            }
        default:
            HStack(spacing: size * 0.15) {
                Circle().fill(MoodprintTheme.ink).frame(width: size * 0.07, height: size * 0.07)
                Circle().fill(MoodprintTheme.ink).frame(width: size * 0.07, height: size * 0.07)
            }
        }
    }

    @ViewBuilder
    private var noseAndMouth: some View {
        switch animal {
        case .penguin, .owl, .chick:
            EmptyView()
        case .pig:
            // 돼지는 별도 주둥이(snout)를 frontBodyDecorations에서 그리므로 코는 생략하지만,
            // 입은 그대로 그려야 한다 — 이전에는 penguin/owl/chick과 함께 묶여
            // EmptyView()를 반환해서 입이 없는 얼굴로 나왔다.
            mouthShape
        case .koala:
            VStack(spacing: size * 0.02) {
                Ellipse().fill(Color(hex: 0x6B6B76)).frame(width: size * 0.14, height: size * 0.1)
                mouthShape
            }
        default:
            VStack(spacing: size * 0.012) {
                Ellipse().fill(noseColor).frame(width: size * 0.07, height: size * 0.055)
                mouthShape
            }
        }
    }

    @ViewBuilder
    private var mouthShape: some View {
        if mood == .happy {
            Capsule()
                .trim(from: 0.5, to: 1)
                .stroke(MoodprintTheme.ink, lineWidth: max(1, size * 0.014))
                .frame(width: size * 0.13, height: size * 0.09)
        } else {
            Capsule()
                .fill(MoodprintTheme.ink)
                .frame(width: size * 0.1, height: max(1, size * 0.012))
        }
    }

    private var noseColor: Color {
        switch animal {
        case .rabbit, .fox: Color(hex: 0xEF8EA3)
        default: Color(hex: 0xC96B6B)
        }
    }
}

// MARK: - Reusable shapes

/// 몽실몽실한 마스코트 실루엣. 가로(폭 .88)에 비해 세로가 짧아(.75) 살짝 눌린 타원처럼 보이던
/// 이전 버전을 세로로 늘려(.86) 원에 가깝게 만들었다 — 귀가 들어갈 위쪽 여백은 그대로 남긴다.
private struct MoodprintBlobShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        let w = rect.width
        let h = rect.height
        path.move(to: CGPoint(x: w * 0.50, y: h * 0.132))
        path.addCurve(
            to: CGPoint(x: w * 0.94, y: h * 0.562),
            control1: CGPoint(x: w * 0.79, y: h * 0.100),
            control2: CGPoint(x: w * 0.96, y: h * 0.293)
        )
        path.addCurve(
            to: CGPoint(x: w * 0.50, y: h * 0.939),
            control1: CGPoint(x: w * 0.96, y: h * 0.842),
            control2: CGPoint(x: w * 0.78, y: h * 0.960)
        )
        path.addCurve(
            to: CGPoint(x: w * 0.06, y: h * 0.562),
            control1: CGPoint(x: w * 0.23, y: h * 0.960),
            control2: CGPoint(x: w * 0.04, y: h * 0.842)
        )
        path.addCurve(
            to: CGPoint(x: w * 0.50, y: h * 0.132),
            control1: CGPoint(x: w * 0.04, y: h * 0.293),
            control2: CGPoint(x: w * 0.21, y: h * 0.100)
        )
        return path
    }
}

/// 뾰족한 귀·부리 등에 두루 쓰이는 위쪽 꼭짓점 삼각형.
private struct TriangleShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.midX, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY))
        path.addLine(to: CGPoint(x: rect.minX, y: rect.maxY))
        path.closeSubpath()
        return path
    }
}

private struct AntlerShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.midX, y: rect.maxY))
        path.addLine(to: CGPoint(x: rect.midX, y: rect.minY))
        path.move(to: CGPoint(x: rect.midX, y: rect.minY + rect.height * 0.35))
        path.addLine(to: CGPoint(x: rect.midX + rect.width * 0.5, y: rect.minY))
        return path
    }
}

/// 사자 갈기: 몸통 뒤에서 사방으로 뻗는 방사형 뭉치. `size`는 펫 전체 프레임 크기.
private struct LionMane: View {
    var color: Color
    var size: CGFloat

    var body: some View {
        // 뾰족한 사각형 광선이 톱니바퀴처럼 보이던 문제를, 서로 겹치는 동그란 뭉치들로 바꿔
        // 부드러운 갈기로 만든다. (Android CommonComponents.kt의 drawLionMane과 동일한 비율)
        let puffCount = 10
        let ringRadius = size * 0.40
        let puffRadius = size * 0.155
        let centerY = size * 0.03
        ZStack {
            ForEach(0..<puffCount, id: \.self) { i in
                let angle = Double(i) / Double(puffCount) * 2 * Double.pi
                Circle()
                    .fill(color)
                    .frame(width: puffRadius * 2, height: puffRadius * 2)
                    .offset(
                        x: ringRadius * CGFloat(cos(angle)),
                        y: centerY + ringRadius * CGFloat(sin(angle))
                    )
            }
        }
    }
}

private extension Color {
    /// 명도를 살짝 낮춰 입체감을 주는 보조 색상을 만든다.
    func darker(by amount: Double) -> Color {
        let uiColor = UIColor(self)
        var hue: CGFloat = 0, saturation: CGFloat = 0, brightness: CGFloat = 0, alpha: CGFloat = 0
        guard uiColor.getHue(&hue, saturation: &saturation, brightness: &brightness, alpha: &alpha) else {
            return self
        }
        return Color(
            hue: Double(hue),
            saturation: Double(saturation),
            brightness: max(0, Double(brightness) - amount),
            opacity: Double(alpha)
        )
    }
}
