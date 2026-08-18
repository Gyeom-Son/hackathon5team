import Foundation

enum MoodEmotion: String, CaseIterable, Identifiable, Codable, Sendable {
    case anxious = "불안"
    case lethargic = "무기력"
    case upset = "속상함"
    case angry = "화남"
    case lonely = "외로움"
    case complicated = "복잡함"

    var id: String { rawValue }
}

enum MoodEnergy: String, CaseIterable, Identifiable, Codable, Sendable {
    case low = "낮음"
    case medium = "보통"
    case high = "높음"

    var id: String { rawValue }
}

enum MoodChange: String, CaseIterable, Identifiable, Codable, Sendable {
    case harder = "더 힘들어요"
    case same = "비슷해요"
    case better = "조금 나아졌어요"
    case muchBetter = "많이 나아졌어요"

    var id: String { rawValue }

    var recommendationWeight: Double {
        switch self {
        case .harder: -0.25
        case .same: 0
        case .better: 0.2
        case .muchBetter: 0.35
        }
    }
}

enum RecoveryActionCategory: String, CaseIterable, Codable, Sendable {
    case sensory = "감각 행동"
    case movement = "움직임"
    case rest = "휴식"
    case expression = "표현"
    case reflection = "회상"
    case environment = "환경"
}

struct ActionRecommendation: Identifiable {
    let action: RecoveryActionRecord
    let score: Double
    let reason: String

    var id: UUID { action.id }
}

struct RewardOutcome: Equatable, Sendable {
    let experienceAwarded: Int
    let fragmentsAwarded: Int
    let wasAlreadyApplied: Bool

    static let alreadyApplied = RewardOutcome(
        experienceAwarded: 0,
        fragmentsAwarded: 0,
        wasAlreadyApplied: true
    )
}
