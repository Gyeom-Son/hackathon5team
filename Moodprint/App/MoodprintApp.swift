import SwiftData
import SwiftUI

@main
struct MoodprintApp: App {
    @StateObject private var bootstrap = AppBootstrap()

    var body: some Scene {
        WindowGroup {
            Group {
                if let container = bootstrap.container {
                    AppContent(container: container)
                } else if let message = bootstrap.errorMessage {
                    ContentUnavailableView {
                        Label("저장소를 열지 못했어요", systemImage: "externaldrive.badge.exclamationmark")
                    } description: {
                        Text(message)
                    } actions: {
                        Button("다시 시도") { bootstrap.load() }
                            .buttonStyle(.borderedProminent)
                    }
                } else {
                    ProgressView("Moodprint를 준비하고 있어요")
                }
            }
            .preferredColorScheme(.light)
            .task { bootstrap.load() }
        }
    }
}

private struct AppContent: View {
    let container: ModelContainer
    @StateObject private var appState: AppState

    init(container: ModelContainer) {
        self.container = container
        _appState = StateObject(wrappedValue: AppState(context: container.mainContext))
    }

    var body: some View {
        RootView()
            .environmentObject(appState)
            .modelContainer(container)
            .task { appState.prepare() }
    }
}
