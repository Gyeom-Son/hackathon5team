import SwiftData
import XCTest
@testable import Moodprint

@MainActor
final class MoodprintCoreTests: XCTestCase {
    private var container: ModelContainer!
    private var context: ModelContext!
    private var moodRepository: SwiftDataMoodRepository!
    private var actionRepository: SwiftDataActionRepository!
    private var petRepository: SwiftDataPetRepository!

    override func setUpWithError() throws {
        container = try MoodprintModelContainer.make(inMemory: true)
        context = container.mainContext
        moodRepository = SwiftDataMoodRepository(context: context)
        actionRepository = SwiftDataActionRepository(context: context)
        petRepository = SwiftDataPetRepository(context: context)
        try actionRepository.seedIfNeeded()
        try petRepository.seedIfNeeded()
    }

    func testMoodAcceptsOneToThreeEmotions() throws {
        XCTAssertNoThrow(
            try moodRepository.add(
                emotions: [.anxious],
                note: nil,
                energy: .low
            )
        )
        XCTAssertNoThrow(
            try moodRepository.add(
                emotions: [.anxious, .lonely, .complicated],
                note: "",
                energy: .medium
            )
        )
    }

    func testMoodRejectsZeroAndFourEmotions() {
        XCTAssertThrowsError(
            try moodRepository.add(emotions: [], note: nil, energy: .low)
        )
        XCTAssertThrowsError(
            try moodRepository.add(
                emotions: [.anxious, .lonely, .angry, .upset],
                note: nil,
                energy: .high
            )
        )
    }

    func testRecommendationUsesAllSelectedEmotions() throws {
        let actions = try actionRepository.fetchCatalog()
        XCTAssertEqual(actions.count, 20)
        let recommendations = RecommendationService().recommend(
            emotions: [.anxious, .upset, .lonely],
            energy: .low,
            actions: actions,
            history: []
        )

        XCTAssertEqual(recommendations.first?.action.title, "좋아하는 음악 한 곡 듣기")
        XCTAssertTrue(recommendations.first?.reason.contains("선택한 감정 3개") == true)
        XCTAssertFalse(recommendations.first?.reason.contains("이전 기록") == true)
    }

    func testSkippedChangeStillRewardsAndRewardIsIdempotent() throws {
        let mood = try moodRepository.add(
            emotions: [.lethargic],
            note: nil,
            energy: .low
        )
        let action = try XCTUnwrap(actionRepository.fetchCatalog().first)
        let result = try actionRepository.addResult(
            sessionID: UUID(),
            moodID: mood.id,
            actionID: action.id,
            change: nil
        )
        let service = RewardService(context: context)

        let first = try service.applyReward(for: result.id)
        let second = try service.applyReward(for: result.id)

        XCTAssertEqual(first.experienceAwarded, 15)
        XCTAssertEqual(first.fragmentsAwarded, 1)
        XCTAssertFalse(first.wasAlreadyApplied)
        XCTAssertTrue(second.wasAlreadyApplied)
        XCTAssertEqual(second.experienceAwarded, 0)
        XCTAssertEqual(try petRepository.fetchPrimary()?.experience, 15)
    }

    func testSameSessionCreatesOnlyOneResult() throws {
        let mood = try moodRepository.add(
            emotions: [.anxious],
            note: nil,
            energy: .medium
        )
        let action = try XCTUnwrap(actionRepository.fetchCatalog().first)
        let sessionID = UUID()

        let first = try actionRepository.addResult(
            sessionID: sessionID,
            moodID: mood.id,
            actionID: action.id,
            change: .same
        )
        let second = try actionRepository.addResult(
            sessionID: sessionID,
            moodID: mood.id,
            actionID: action.id,
            change: .muchBetter
        )

        XCTAssertEqual(first.id, second.id)
        XCTAssertEqual(try actionRepository.fetchResults().count, 1)
    }
}
