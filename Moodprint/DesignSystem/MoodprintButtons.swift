import SwiftUI

struct PrimaryButton: View {
    private let title: LocalizedStringKey
    private let systemImage: String?
    private let action: () -> Void

    init(
        _ title: LocalizedStringKey,
        systemImage: String? = nil,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.systemImage = systemImage
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            Group {
                if let systemImage {
                    Label(title, systemImage: systemImage)
                } else {
                    Text(title)
                }
            }
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(MoodprintPrimaryButtonStyle())
    }
}

struct SecondaryButton: View {
    private let title: LocalizedStringKey
    private let systemImage: String?
    private let action: () -> Void

    init(
        _ title: LocalizedStringKey,
        systemImage: String? = nil,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.systemImage = systemImage
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            Group {
                if let systemImage {
                    Label(title, systemImage: systemImage)
                } else {
                    Text(title)
                }
            }
            .frame(maxWidth: .infinity)
        }
        .buttonStyle(MoodprintSecondaryButtonStyle())
    }
}

struct MoodprintPrimaryButtonStyle: ButtonStyle {
    @Environment(\.isEnabled) private var isEnabled

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .foregroundStyle(.white)
            .padding(.horizontal, 18)
            .frame(minHeight: MoodprintTheme.buttonHeight)
            .background(
                backgroundColor(isPressed: configuration.isPressed),
                in: RoundedRectangle(
                    cornerRadius: MoodprintTheme.controlCornerRadius,
                    style: .continuous
                )
            )
            .contentShape(Rectangle())
            .scaleEffect(configuration.isPressed ? 0.985 : 1)
            .animation(.easeOut(duration: 0.12), value: configuration.isPressed)
    }

    private func backgroundColor(isPressed: Bool) -> Color {
        guard isEnabled else { return MoodprintTheme.primary.opacity(0.42) }
        return isPressed ? MoodprintTheme.primaryPressed : MoodprintTheme.primary
    }
}

struct MoodprintSecondaryButtonStyle: ButtonStyle {
    @Environment(\.isEnabled) private var isEnabled

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .foregroundStyle(isEnabled ? MoodprintTheme.ink : MoodprintTheme.secondaryText)
            .padding(.horizontal, 18)
            .frame(minHeight: MoodprintTheme.buttonHeight)
            .background(
                MoodprintTheme.surface.opacity(configuration.isPressed ? 0.62 : 1),
                in: RoundedRectangle(
                    cornerRadius: MoodprintTheme.controlCornerRadius,
                    style: .continuous
                )
            )
            .overlay {
                RoundedRectangle(
                    cornerRadius: MoodprintTheme.controlCornerRadius,
                    style: .continuous
                )
                .stroke(MoodprintTheme.border, lineWidth: 1)
            }
            .contentShape(Rectangle())
            .opacity(isEnabled ? 1 : 0.55)
    }
}
