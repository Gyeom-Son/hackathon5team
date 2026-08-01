import SwiftUI

struct RewardView: View {
    @EnvironmentObject private var appState: AppState

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Eyebrow("행동 완료")
                PetView(size: 158, mood: .happy)
                MoodprintTag(
                    text: "성장 경험치 +\(appState.rewardOutcome?.experienceAwarded ?? 15)",
                    color: MoodprintTheme.warm,
                    systemImage: "sparkles"
                )
                Text("몽실이가 조금 성장했어요")
                    .font(.title2.bold())
                    .multilineTextAlignment(.center)
                Text("어떤 변화든 나를 이해하는 중요한 기록이에요.")
                    .foregroundStyle(MoodprintTheme.secondaryText)
                    .multilineTextAlignment(.center)
                if (appState.rewardOutcome?.fragmentsAwarded ?? 0) > 0 {
                    VStack(spacing: 5) {
                        Text("새로운 도감 조각 발견!")
                            .font(.headline)
                        Text("행동을 완료해 마음 생물 조각을 얻었어요.")
                    }
                    .frame(maxWidth: .infinity)
                    .moodprintCard(background: MoodprintTheme.coral)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(22)
        }
        .safeAreaInset(edge: .bottom) {
            VStack(spacing: 10) {
            PrimaryButton("도감 확인하기") {
                appState.showCollection()
            }
            .accessibilityIdentifier("reward.collection")
            SecondaryButton("홈으로") {
                appState.returnHome()
            }
            }
            .padding(22)
            .background(.ultraThinMaterial)
        }
        .background(MoodprintTheme.background)
    }
}
