import XCTest

final class MoodprintFlowUITests: XCTestCase {
    func testOnboardingReachesCheckIn() {
        let app = XCUIApplication()
        app.launchArguments = ["-uiTestingReset"]
        app.launch()

        app.buttons["welcome.start"].tap()
        app.buttons["introduction.continue"].tap()
        app.buttons["profile.continue"].tap()
        app.buttons["home.checkin"].tap()

        XCTAssertTrue(app.buttons["checkin.submit"].exists)
        XCTAssertFalse(app.buttons["checkin.submit"].isEnabled)
    }

    func testThreeEmotionSelectionDisablesFourth() {
        let app = XCUIApplication()
        app.launchArguments = ["-uiTestingReset"]
        app.launch()
        app.buttons["welcome.start"].tap()
        app.buttons["introduction.continue"].tap()
        app.buttons["profile.continue"].tap()
        app.buttons["home.checkin"].tap()

        app.buttons["emotion.불안"].tap()
        app.buttons["emotion.무기력"].tap()
        app.buttons["emotion.속상함"].tap()

        XCTAssertFalse(app.buttons["emotion.화남"].isEnabled)
        XCTAssertTrue(app.buttons["emotion.불안"].isEnabled)
        XCTAssertTrue(app.buttons["checkin.submit"].isEnabled)
    }

    func testCompleteFlowCanSkipChangeAndReachCollection() {
        let app = XCUIApplication()
        app.launchArguments = ["-uiTestingReset"]
        app.launch()
        app.buttons["welcome.start"].tap()
        app.buttons["introduction.continue"].tap()
        app.buttons["profile.continue"].tap()
        app.buttons["home.checkin"].tap()
        app.buttons["emotion.불안"].tap()
        app.buttons["checkin.submit"].tap()

        XCTAssertTrue(app.buttons["recommendation.start"].waitForExistence(timeout: 3))
        app.buttons["recommendation.start"].tap()
        XCTAssertTrue(app.buttons["action.complete"].waitForExistence(timeout: 3))
        app.buttons["action.complete"].tap()
        XCTAssertTrue(app.buttons["change.skip"].waitForExistence(timeout: 3))
        app.buttons["change.skip"].tap()
        XCTAssertTrue(app.buttons["reward.collection"].waitForExistence(timeout: 3))
        app.buttons["reward.collection"].tap()

        XCTAssertTrue(app.navigationBars["도감"].waitForExistence(timeout: 3))
    }
}
