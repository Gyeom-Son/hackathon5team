import Foundation
import SwiftData

@Model
final class MoodRecord {
    @Attribute(.unique) var id: UUID
    var createdAt: Date
    var emotionRawValues: [String]
    var note: String?
    var energyRawValue: String

    init(
        id: UUID = UUID(),
        createdAt: Date = .now,
        emotions: [MoodEmotion],
        note: String?,
        energy: MoodEnergy
    ) {
        precondition((1...3).contains(emotions.count), "감정은 1개 이상 3개 이하로 선택해야 합니다.")
        self.id = id
        self.createdAt = createdAt
        self.emotionRawValues = emotions.map(\.rawValue)
        self.note = note?.trimmingCharacters(in: .whitespacesAndNewlines).nilIfEmpty
        self.energyRawValue = energy.rawValue
    }

    var emotions: [MoodEmotion] {
        emotionRawValues.compactMap(MoodEmotion.init(rawValue:))
    }

    var energy: MoodEnergy {
        MoodEnergy(rawValue: energyRawValue) ?? .medium
    }
}

@Model
final class RecoveryActionRecord {
    @Attribute(.unique) var id: UUID
    var title: String
    var instruction: String
    var durationSeconds: Int
    var categoryRawValue: String
    var symbol: String
    var supportedEmotionRawValues: [String]
    var supportedEnergyRawValues: [String]
    var catalogOrder: Int
    var detailPrompt: String?
    var detailPlaceholder: String?

    init(
        id: UUID,
        title: String,
        instruction: String,
        durationSeconds: Int,
        category: RecoveryActionCategory,
        symbol: String,
        supportedEmotions: [MoodEmotion],
        supportedEnergies: [MoodEnergy],
        catalogOrder: Int,
        detailPrompt: String? = nil,
        detailPlaceholder: String? = nil
    ) {
        self.id = id
        self.title = title
        self.instruction = instruction
        self.durationSeconds = durationSeconds
        self.categoryRawValue = category.rawValue
        self.symbol = symbol
        self.supportedEmotionRawValues = supportedEmotions.map(\.rawValue)
        self.supportedEnergyRawValues = supportedEnergies.map(\.rawValue)
        self.catalogOrder = catalogOrder
        self.detailPrompt = detailPrompt
        self.detailPlaceholder = detailPlaceholder
    }

    var category: RecoveryActionCategory {
        RecoveryActionCategory(rawValue: categoryRawValue) ?? .rest
    }

    var supportedEmotions: Set<MoodEmotion> {
        Set(supportedEmotionRawValues.compactMap(MoodEmotion.init(rawValue:)))
    }

    var supportedEnergies: Set<MoodEnergy> {
        Set(supportedEnergyRawValues.compactMap(MoodEnergy.init(rawValue:)))
    }
}

@Model
final class ActionResultRecord {
    @Attribute(.unique) var id: UUID
    @Attribute(.unique) var sessionID: UUID
    var moodID: UUID
    var actionID: UUID
    var completedAt: Date
    var changeRawValue: String?
    var detailNote: String?

    init(
        id: UUID = UUID(),
        sessionID: UUID,
        moodID: UUID,
        actionID: UUID,
        completedAt: Date = .now,
        change: MoodChange?,
        detailNote: String? = nil
    ) {
        self.id = id
        self.sessionID = sessionID
        self.moodID = moodID
        self.actionID = actionID
        self.completedAt = completedAt
        self.changeRawValue = change?.rawValue
        self.detailNote = detailNote?.trimmingCharacters(in: .whitespacesAndNewlines).nilIfEmpty
    }

    var change: MoodChange? {
        changeRawValue.flatMap(MoodChange.init(rawValue:))
    }
}

@Model
final class RewardRecord {
    @Attribute(.unique) var id: UUID
    @Attribute(.unique) var resultID: UUID
    var experienceAwarded: Int
    var fragmentsAwarded: Int
    var appliedAt: Date

    init(
        id: UUID = UUID(),
        resultID: UUID,
        experienceAwarded: Int,
        fragmentsAwarded: Int,
        appliedAt: Date = .now
    ) {
        self.id = id
        self.resultID = resultID
        self.experienceAwarded = experienceAwarded
        self.fragmentsAwarded = fragmentsAwarded
        self.appliedAt = appliedAt
    }
}

@Model
final class PetProgressRecord {
    @Attribute(.unique) var id: UUID
    var name: String
    var colorName: String
    var level: Int
    var experience: Int
    var fragments: Int
    var requiredFragments: Int
    var isUnlocked: Bool
    var isPrimary: Bool

    init(
        id: UUID,
        name: String,
        colorName: String,
        level: Int,
        experience: Int,
        fragments: Int,
        requiredFragments: Int = 3,
        isUnlocked: Bool,
        isPrimary: Bool = false
    ) {
        self.id = id
        self.name = name
        self.colorName = colorName
        self.level = level
        self.experience = experience
        self.fragments = fragments
        self.requiredFragments = requiredFragments
        self.isUnlocked = isUnlocked
        self.isPrimary = isPrimary
    }

    func addExperience(_ amount: Int) {
        experience += max(0, amount)
        level = max(1, experience / 100 + 1)
    }

    func addFragments(_ amount: Int) {
        fragments = min(requiredFragments, fragments + max(0, amount))
        if fragments >= requiredFragments {
            isUnlocked = true
        }
    }
}

private extension String {
    var nilIfEmpty: String? { isEmpty ? nil : self }
}
