import SwiftUI

struct MoodprintCardModifier: ViewModifier {
    let background: Color
    let padding: CGFloat

    func body(content: Content) -> some View {
        content
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(padding)
            .background(
                background,
                in: RoundedRectangle(
                    cornerRadius: MoodprintTheme.cardCornerRadius,
                    style: .continuous
                )
            )
    }
}

extension View {
    func moodprintCard(
        background: Color = MoodprintTheme.surface,
        padding: CGFloat = 18
    ) -> some View {
        modifier(MoodprintCardModifier(background: background, padding: padding))
    }
}
