import SwiftUI

struct RecommendationView: View {
    @EnvironmentObject private var appState: AppState

    var body: some View {
        NavigationStack {
            ScrollView {
                if let recommendation = appState.currentRecommendation {
                    VStack(spacing: 20) {
                        VStack(spacing: 8) {
                            Text("지금 할 수 있는 하나")
                                .font(.title.bold())
                            Text("추천 \(appState.recommendationIndex + 1)/\(appState.recommendations.count)")
                                .font(.subheadline.weight(.semibold))
                                .foregroundStyle(MoodprintTheme.primary)
                        }
                        .frame(maxWidth: .infinity)

                        PetView(
                            size: 150,
                            mood: .calm,
                            animal: appState.primaryPet?.animal ?? .cat,
                            stage: appState.primaryPet?.growthStage ?? 1,
                            name: appState.primaryPet?.name ?? AnimalKind.cat.koreanName
                        )

                        VStack(alignment: .leading, spacing: 14) {
                            Label(recommendation.action.title, systemImage: recommendation.action.symbol)
                                .font(.title3.bold())
                            Text(recommendation.action.instruction)
                                .foregroundStyle(MoodprintTheme.secondaryText)
                            Label(durationText(recommendation.action.durationSeconds), systemImage: "timer")
                                .font(.subheadline.weight(.semibold))
                                .foregroundStyle(MoodprintTheme.primary)
                        }
                        .moodprintCard()

                        Text(recommendation.reason)
                            .font(.subheadline)
                            .foregroundStyle(MoodprintTheme.secondaryText)
                            .moodprintCard(background: MoodprintTheme.mint)
                    }
                    .padding(20)
                }
            }
            .background(MoodprintTheme.background)
            .safeAreaInset(edge: .bottom) {
                VStack(spacing: 10) {
                    PrimaryButton("이 행동 시작하기") {
                        appState.route = .action
                    }
                    SecondaryButton("다른 행동 보기", systemImage: "arrow.triangle.2.circlepath") {
                        appState.showNextRecommendation()
                    }
                }
                .padding(20)
                .background(.ultraThinMaterial)
            }
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("뒤로", systemImage: "chevron.left") {
                        appState.route = .checkIn
                    }
                }
            }
        }
    }

    private func durationText(_ seconds: Int) -> String {
        let minutes = max(1, Int(ceil(Double(seconds) / 60)))
        return "약 \(minutes)분"
    }
}
