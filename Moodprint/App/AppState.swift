import Foundation
import SwiftData

enum AppRoute {
    case welcome
    case introduction
    case profileSetup
    case main
    case checkIn
    case recommendation
    case action
    case reward
}

enum MainTab: Hashable {
    case home
    case collection
    case records
}

@MainActor
final class AppState: ObservableObject {
    @Published var route: AppRoute
    @Published var selectedTab: MainTab = .home
    @Published private(set) var nickname: String
    @Published private(set) var moods: [MoodRecord] = []
    @Published private(set) var actions: [RecoveryActionRecord] = []
    @Published private(set) var results: [ActionResultRecord] = []
    @Published private(set) var pets: [PetProgressRecord] = []
    @Published private(set) var recommendations: [ActionRecommendation] = []
    @Published private(set) var recommendationIndex = 0
    @Published private(set) var rewardOutcome: RewardOutcome?
    @Published var errorMessage: String?

    private let moodRepository: MoodRepository
    private let actionRepository: ActionRepository
    private let petRepository: PetRepository
    private let recommendationService = RecommendationService()
    private let rewardService: RewardService
    private var currentMood: MoodRecord?
    private var currentSessionID = UUID()
    private var hasCompletedCurrentSession = false

    init(context: ModelContext) {
        if ProcessInfo.processInfo.arguments.contains("-uiTestingReset") {
            UserDefaults.standard.removeObject(forKey: Self.nicknameKey)
            UserDefaults.standard.removeObject(forKey: Self.onboardingKey)
        }
        let savedNickname = UserDefaults.standard.string(forKey: Self.nicknameKey) ?? ""
        let completed = UserDefaults.standard.bool(forKey: Self.onboardingKey)
        route = completed ? .main : .welcome
        nickname = savedNickname
        moodRepository = SwiftDataMoodRepository(context: context)
        actionRepository = SwiftDataActionRepository(context: context)
        petRepository = SwiftDataPetRepository(context: context)
        rewardService = RewardService(context: context)
    }

    func completeOnboarding(nickname: String) {
        updateNickname(nickname)
        UserDefaults.standard.set(true, forKey: Self.onboardingKey)
        route = .main
    }

    func updateNickname(_ value: String) {
        let trimmed = value.trimmingCharacters(in: .whitespacesAndNewlines)
        nickname = trimmed.isEmpty ? "마음 여행자" : String(trimmed.prefix(12))
        UserDefaults.standard.set(nickname, forKey: Self.nicknameKey)
    }

    var currentRecommendation: ActionRecommendation? {
        guard recommendations.indices.contains(recommendationIndex) else { return nil }
        return recommendations[recommendationIndex]
    }

    var primaryPet: PetProgressRecord? {
        pets.first(where: \.isPrimary)
    }

    func prepare() {
        perform {
            try actionRepository.seedIfNeeded()
            try petRepository.seedIfNeeded()
            try refresh()
        }
    }

    func saveMood(emotions: [MoodEmotion], note: String, energy: MoodEnergy) {
        perform {
            let mood = try moodRepository.add(
                emotions: emotions,
                note: note,
                energy: energy
            )
            currentMood = mood
            currentSessionID = UUID()
            hasCompletedCurrentSession = false
            try refresh()
            recommendations = recommendationService.recommend(
                emotions: emotions,
                energy: energy,
                actions: actions,
                history: results
            )
            recommendationIndex = 0
            guard !recommendations.isEmpty else {
                throw AppStateError.noRecommendation
            }
            route = .recommendation
        }
    }

    func showNextRecommendation() {
        guard !recommendations.isEmpty else { return }
        recommendationIndex = (recommendationIndex + 1) % recommendations.count
    }

    func finishCurrentAction(change: MoodChange?, detailNote: String? = nil) {
        guard !hasCompletedCurrentSession else { return }
        hasCompletedCurrentSession = true
        do {
            errorMessage = nil
            guard let mood = currentMood,
                  let action = currentRecommendation?.action else {
                throw AppStateError.missingActiveSession
            }
            let result = try actionRepository.addResult(
                sessionID: currentSessionID,
                moodID: mood.id,
                actionID: action.id,
                change: change,
                detailNote: detailNote
            )
            rewardOutcome = try rewardService.applyReward(for: result.id)
            try refresh()
            route = .reward
        } catch {
            hasCompletedCurrentSession = false
            errorMessage = error.localizedDescription
        }
    }

    func returnHome() {
        currentMood = nil
        recommendations = []
        recommendationIndex = 0
        hasCompletedCurrentSession = false
        selectedTab = .home
        route = .main
    }

    func showCollection() {
        selectedTab = .collection
        route = .main
    }

    /// 해금된 동물을 홈 화면 대표 동반자로 바꾼다. 펫마다 레벨·경험치는 그대로 따로 유지된다.
    func setPrimaryPet(_ pet: PetProgressRecord) {
        guard pet.isUnlocked, !pet.isPrimary else { return }
        perform {
            try petRepository.setPrimary(id: pet.id)
            try refresh()
        }
    }

    func retry() {
        errorMessage = nil
        prepare()
    }

    private func refresh() throws {
        moods = try moodRepository.fetchAll()
        actions = try actionRepository.fetchCatalog()
        results = try actionRepository.fetchResults()
        pets = try petRepository.fetchAll()
    }

    private func perform(_ work: () throws -> Void) {
        do {
            errorMessage = nil
            try work()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

private extension AppState {
    static let nicknameKey = "moodprint.profile.nickname"
    static let onboardingKey = "moodprint.onboarding.completed"
}

private enum AppStateError: LocalizedError {
    case noRecommendation
    case missingActiveSession

    var errorDescription: String? {
        switch self {
        case .noRecommendation:
            "추천할 행동을 준비하지 못했어요. 다시 시도해 주세요."
        case .missingActiveSession:
            "진행 중인 기록을 찾지 못했어요. 홈에서 다시 시작해 주세요."
        }
    }
}
