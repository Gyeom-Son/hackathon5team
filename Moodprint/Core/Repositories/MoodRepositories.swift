import Foundation
import SwiftData

enum MoodRepositoryError: LocalizedError {
    case invalidEmotionCount
    case duplicateEmotions

    var errorDescription: String? {
        switch self {
        case .invalidEmotionCount:
            "감정은 1개 이상 3개 이하로 선택해 주세요."
        case .duplicateEmotions:
            "같은 감정을 중복해서 선택할 수 없습니다."
        }
    }
}

@MainActor
protocol MoodRepository {
    @discardableResult
    func add(
        emotions: [MoodEmotion],
        note: String?,
        energy: MoodEnergy
    ) throws -> MoodRecord

    func fetchAll() throws -> [MoodRecord]
}

@MainActor
protocol ActionRepository {
    func seedIfNeeded() throws
    func fetchCatalog() throws -> [RecoveryActionRecord]
    func fetchResults() throws -> [ActionResultRecord]
    func fetchResult(id: UUID) throws -> ActionResultRecord?

    @discardableResult
    func addResult(
        sessionID: UUID,
        moodID: UUID,
        actionID: UUID,
        change: MoodChange?,
        detailNote: String?
    ) throws -> ActionResultRecord
}

@MainActor
protocol PetRepository {
    func seedIfNeeded() throws
    func fetchAll() throws -> [PetProgressRecord]
    func fetchPrimary() throws -> PetProgressRecord?
}

@MainActor
final class SwiftDataMoodRepository: MoodRepository {
    private let context: ModelContext

    init(context: ModelContext) {
        self.context = context
    }

    func add(
        emotions: [MoodEmotion],
        note: String?,
        energy: MoodEnergy
    ) throws -> MoodRecord {
        guard (1...3).contains(emotions.count) else {
            throw MoodRepositoryError.invalidEmotionCount
        }
        guard Set(emotions).count == emotions.count else {
            throw MoodRepositoryError.duplicateEmotions
        }

        let record = MoodRecord(
            emotions: emotions,
            note: note,
            energy: energy
        )
        context.insert(record)
        try context.save()
        return record
    }

    func fetchAll() throws -> [MoodRecord] {
        let descriptor = FetchDescriptor<MoodRecord>(
            sortBy: [SortDescriptor(\.createdAt, order: .reverse)]
        )
        return try context.fetch(descriptor)
    }
}

@MainActor
final class SwiftDataActionRepository: ActionRepository {
    private let context: ModelContext

    init(context: ModelContext) {
        self.context = context
    }

    func seedIfNeeded() throws {
        let existing = try context.fetch(FetchDescriptor<RecoveryActionRecord>())
        let existingByID = Dictionary(uniqueKeysWithValues: existing.map { ($0.id, $0) })
        for seed in ActionCatalogSeed.actions {
            if let record = existingByID[seed.id] {
                record.title = seed.title
                record.instruction = seed.instruction
                record.durationSeconds = seed.durationSeconds
                record.categoryRawValue = seed.categoryRawValue
                record.symbol = seed.symbol
                record.supportedEmotionRawValues = seed.supportedEmotionRawValues
                record.supportedEnergyRawValues = seed.supportedEnergyRawValues
                record.catalogOrder = seed.catalogOrder
                record.detailPrompt = seed.detailPrompt
                record.detailPlaceholder = seed.detailPlaceholder
            } else {
                context.insert(seed)
            }
        }
        try context.save()
    }

    func fetchCatalog() throws -> [RecoveryActionRecord] {
        let descriptor = FetchDescriptor<RecoveryActionRecord>(
            sortBy: [SortDescriptor(\.catalogOrder)]
        )
        return try context.fetch(descriptor)
    }

    func fetchResults() throws -> [ActionResultRecord] {
        let descriptor = FetchDescriptor<ActionResultRecord>(
            sortBy: [SortDescriptor(\.completedAt, order: .reverse)]
        )
        return try context.fetch(descriptor)
    }

    func fetchResult(id: UUID) throws -> ActionResultRecord? {
        try context.fetch(FetchDescriptor<ActionResultRecord>())
            .first(where: { $0.id == id })
    }

    func addResult(
        sessionID: UUID,
        moodID: UUID,
        actionID: UUID,
        change: MoodChange?,
        detailNote: String? = nil
    ) throws -> ActionResultRecord {
        if let existing = try context.fetch(FetchDescriptor<ActionResultRecord>())
            .first(where: { $0.sessionID == sessionID }) {
            return existing
        }
        let record = ActionResultRecord(
            sessionID: sessionID,
            moodID: moodID,
            actionID: actionID,
            change: change,
            detailNote: detailNote
        )
        context.insert(record)
        try context.save()
        return record
    }
}

@MainActor
final class SwiftDataPetRepository: PetRepository {
    private let context: ModelContext

    init(context: ModelContext) {
        self.context = context
    }

    func seedIfNeeded() throws {
        let count = try context.fetchCount(FetchDescriptor<PetProgressRecord>())
        guard count == 0 else { return }
        PetCatalogSeed.pets.forEach(context.insert)
        try context.save()
    }

    func fetchAll() throws -> [PetProgressRecord] {
        let records = try context.fetch(FetchDescriptor<PetProgressRecord>())
        return records.sorted {
            if $0.isPrimary != $1.isPrimary { return $0.isPrimary }
            if $0.isUnlocked != $1.isUnlocked { return $0.isUnlocked }
            return $0.name < $1.name
        }
    }

    func fetchPrimary() throws -> PetProgressRecord? {
        try context.fetch(FetchDescriptor<PetProgressRecord>())
            .first(where: \.isPrimary)
    }
}
