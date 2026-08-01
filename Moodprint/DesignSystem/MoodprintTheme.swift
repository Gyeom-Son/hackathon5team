import SwiftUI

/// Moodprint의 공통 색상과 레이아웃 토큰입니다.
///
/// 시스템 폰트를 그대로 사용해 Dynamic Type과 한국어 시스템 폰트 대체를 지원합니다.
enum MoodprintTheme {
    static let primary = Color(red: 0.44, green: 0.36, blue: 0.68)
    static let primaryPressed = Color(red: 0.36, green: 0.29, blue: 0.58)

    static let background = Color(red: 0.97, green: 0.95, blue: 0.99)
    static let surface = Color(uiColor: .systemBackground)
    static let softPurple = Color(red: 0.92, green: 0.89, blue: 0.97)
    static let mint = Color(red: 0.84, green: 0.94, blue: 0.91)
    static let coral = Color(red: 0.97, green: 0.86, blue: 0.83)
    static let warm = Color(red: 0.97, green: 0.91, blue: 0.73)

    static let ink = Color(red: 0.18, green: 0.16, blue: 0.25)
    static let secondaryText = Color(red: 0.43, green: 0.41, blue: 0.49)
    static let border = Color(red: 0.84, green: 0.81, blue: 0.88)

    static let petPurple = Color(red: 0.76, green: 0.68, blue: 0.90)
    static let petMint = Color(red: 0.62, green: 0.84, blue: 0.77)
    static let petCoral = Color(red: 0.92, green: 0.66, blue: 0.62)
    static let petYellow = Color(red: 0.90, green: 0.77, blue: 0.42)

    static let minimumTouchTarget: CGFloat = 44
    static let buttonHeight: CGFloat = 52
    static let controlCornerRadius: CGFloat = 15
    static let cardCornerRadius: CGFloat = 20
}
