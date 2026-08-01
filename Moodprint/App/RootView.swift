import SwiftUI

struct RootView: View {
    @EnvironmentObject private var appState: AppState

    var body: some View {
        ZStack {
            MoodprintTheme.background.ignoresSafeArea()

            switch appState.route {
            case .welcome:
                WelcomeView { appState.route = .introduction }
            case .introduction:
                IntroductionView { appState.route = .profileSetup }
            case .profileSetup:
                ProfileSetupView()
            case .main:
                MainTabView()
            case .checkIn:
                CheckInView()
            case .recommendation:
                RecommendationView()
            case .action:
                if let action = appState.currentRecommendation?.action {
                    ActionExecutionView(action: action)
                }
            case .reward:
                RewardView()
            }
        }
        .animation(.easeInOut(duration: 0.22), value: appState.route)
        .alert(
            "잠시 문제가 생겼어요",
            isPresented: Binding(
                get: { appState.errorMessage != nil },
                set: { if !$0 { appState.errorMessage = nil } }
            )
        ) {
            Button("다시 시도") { appState.retry() }
            Button("확인", role: .cancel) {}
        } message: {
            Text(appState.errorMessage ?? "")
        }
    }
}
