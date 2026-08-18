import Foundation
import SwiftData

enum RewardServiceError: LocalizedError {
    case resultNotFound
    case primaryPetNotFound

    var errorDescription: String? {
        switch self {
        case .resultNotFound:
            "완료한 행동 기록을 찾을 수 없습니다."
        case .primaryPetNotFound:
            "성장시킬 펫을 찾을 수 없습니다."
        }
    }
}

@MainActor
final class RewardService {
    static let experiencePerCompletion = 15
    static let fragmentsPerCompletion = 1

    private let context: ModelContext

    init(context: ModelContext) {
        self.context = context
    }

    /// 변화 기록을 생략했거나 변화가 없더라도 완료된 행동에는 같은 보상을 지급한다.
    /// 별도 RewardRecord의 unique resultID로 같은 결과의 보상이 중복 적용되지 않게 한다.
    func applyReward(for resultID: UUID) throws -> RewardOutcome {
        let results = try context.fetch(FetchDescriptor<ActionResultRecord>())
        guard let result = results.first(where: { $0.id == resultID }) else {
            throw RewardServiceError.resultNotFound
        }
        let rewards = try context.fetch(FetchDescriptor<RewardRecord>())
        guard !rewards.contains(where: { $0.resultID == resultID }) else {
            return .alreadyApplied
        }

        let pets = try context.fetch(FetchDescriptor<PetProgressRecord>())
        guard let primaryPet = pets.first(where: \.isPrimary) else {
            throw RewardServiceError.primaryPetNotFound
        }

        let collectionPet = pets
            .filter { !$0.isPrimary && !$0.isUnlocked }
            .sorted { $0.name < $1.name }
            .first

        primaryPet.addExperience(Self.experiencePerCompletion)
        collectionPet?.addFragments(Self.fragmentsPerCompletion)
        let fragmentsAwarded = collectionPet == nil ? 0 : Self.fragmentsPerCompletion
        context.insert(
            RewardRecord(
                resultID: result.id,
                experienceAwarded: Self.experiencePerCompletion,
                fragmentsAwarded: fragmentsAwarded
            )
        )

        do {
            try context.save()
        } catch {
            context.rollback()
            throw error
        }

        return RewardOutcome(
            experienceAwarded: Self.experiencePerCompletion,
            fragmentsAwarded: fragmentsAwarded,
            wasAlreadyApplied: false
        )
    }
}
