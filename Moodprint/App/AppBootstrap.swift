import SwiftData
import SwiftUI

@MainActor
final class AppBootstrap: ObservableObject {
    @Published private(set) var container: ModelContainer?
    @Published private(set) var errorMessage: String?

    func load() {
        guard container == nil else { return }
        do {
            errorMessage = nil
            container = try MoodprintModelContainer.make()
        } catch {
            errorMessage = "기기에 기록 공간을 준비하지 못했어요. 잠시 후 다시 시도해 주세요.\n\(error.localizedDescription)"
        }
    }
}
