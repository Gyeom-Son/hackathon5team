import SwiftUI

struct WelcomeView: View {
    let onStart: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 22) {
                Eyebrow("MOODPRINT")
                PetView(size: 150, mood: .happy, animal: PetCatalogSeed.primaryAnimal)
                Text("나에게 맞는\n작은 회복 행동을 발견해요")
                    .font(.largeTitle.bold())
                    .minimumScaleFactor(0.75)
                    .multilineTextAlignment(.center)
                Text("감정과 행동의 변화를 기록하며\n나만의 마음 생물 도감을 채워보세요.")
                    .font(.body)
                    .foregroundStyle(MoodprintTheme.secondaryText)
                    .multilineTextAlignment(.center)
            }
            .frame(maxWidth: .infinity)
            .padding(.top, 100)
            .padding(.horizontal, 24)
        }
        .safeAreaInset(edge: .bottom) {
            PrimaryButton("시작하기", action: onStart)
                .accessibilityIdentifier("welcome.start")
                .padding(24)
                .background(.ultraThinMaterial)
        }
    }
}

struct IntroductionView: View {
    let onContinue: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                PetView(size: 158, mood: .calm, animal: PetCatalogSeed.primaryAnimal)
                Text("안녕! 나는 \(PetCatalogSeed.primaryAnimal.koreanName)야.")
                    .font(.title2.bold())
                Text("네 마음의 변화를 함께 기록할 동반자야.")
                    .font(.body)
                    .multilineTextAlignment(.center)
                Text("작은 행동을 실험할수록\n새로운 마음 생물을 발견할 수 있어.")
                    .foregroundStyle(MoodprintTheme.secondaryText)
                    .multilineTextAlignment(.center)
            }
            .frame(maxWidth: .infinity)
            .padding(.top, 120)
            .padding(.horizontal, 24)
        }
        .safeAreaInset(edge: .bottom) {
            PrimaryButton("내 마음 기록하기", action: onContinue)
                .accessibilityIdentifier("introduction.continue")
                .padding(24)
                .background(.ultraThinMaterial)
        }
    }
}

struct ProfileSetupView: View {
    @EnvironmentObject private var appState: AppState
    @State private var nickname = ""

    var body: some View {
        VStack(spacing: 22) {
            Spacer()
            Image(systemName: "person.crop.circle.fill")
                .font(.system(size: 68))
                .foregroundStyle(MoodprintTheme.primary)
                .accessibilityHidden(true)
            Text("어떻게 불러드릴까요?")
                .font(.title.bold())
            Text("계정을 만들지 않고 이 기기에서만 사용할 닉네임이에요.")
                .foregroundStyle(MoodprintTheme.secondaryText)
                .multilineTextAlignment(.center)
            TextField("닉네임", text: $nickname)
                .textInputAutocapitalization(.never)
                .submitLabel(.done)
                .padding(14)
                .background(MoodprintTheme.surface, in: RoundedRectangle(cornerRadius: 14))
                .overlay {
                    RoundedRectangle(cornerRadius: 14)
                        .stroke(MoodprintTheme.border)
                }
                .accessibilityIdentifier("profile.nickname")
            Text("입력하지 않으면 ‘마음 여행자’로 시작해요.")
                .font(.caption)
                .foregroundStyle(MoodprintTheme.secondaryText)
            Spacer()
            PrimaryButton("로그인 없이 시작하기") {
                appState.completeOnboarding(nickname: nickname)
            }
            .accessibilityIdentifier("profile.continue")
        }
        .padding(24)
    }
}
