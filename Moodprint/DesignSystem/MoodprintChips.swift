import SwiftUI

struct ChoiceChip: View {
    let title: LocalizedStringKey
    let isSelected: Bool
    var isEnabled: Bool = true
    let action: () -> Void

    init(
        _ title: LocalizedStringKey,
        isSelected: Bool,
        isEnabled: Bool = true,
        action: @escaping () -> Void
    ) {
        self.title = title
        self.isSelected = isSelected
        self.isEnabled = isEnabled
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                Text(title)
                if isSelected {
                    Image(systemName: "checkmark")
                        .font(.caption.bold())
                        .accessibilityHidden(true)
                }
            }
            .font(.subheadline.weight(.semibold))
            .foregroundStyle(isSelected ? .white : MoodprintTheme.ink)
            .padding(.horizontal, 12)
            .frame(maxWidth: .infinity, minHeight: MoodprintTheme.minimumTouchTarget)
            .background(
                isSelected ? MoodprintTheme.primary : MoodprintTheme.surface,
                in: RoundedRectangle(cornerRadius: 13, style: .continuous)
            )
            .overlay {
                RoundedRectangle(cornerRadius: 13, style: .continuous)
                    .stroke(
                        isSelected ? MoodprintTheme.primary : MoodprintTheme.border,
                        lineWidth: isSelected ? 2 : 1
                    )
            }
        }
        .buttonStyle(.plain)
        .disabled(!isEnabled)
        .opacity(isEnabled ? 1 : 0.45)
        .accessibilityValue(isSelected ? Text("선택됨") : Text("선택 안 됨"))
        .accessibilityHint(isEnabled ? Text("두 번 탭하여 선택 상태를 변경합니다") : Text("선택 한도에 도달했습니다"))
    }
}

struct MoodprintTag: View {
    let text: LocalizedStringKey
    var color: Color = MoodprintTheme.softPurple
    var systemImage: String?

    var body: some View {
        HStack(spacing: 5) {
            if let systemImage {
                Image(systemName: systemImage)
                    .accessibilityHidden(true)
            }
            Text(text)
        }
        .font(.caption.weight(.semibold))
        .foregroundStyle(MoodprintTheme.ink)
        .padding(.horizontal, 11)
        .padding(.vertical, 7)
        .background(color, in: Capsule())
        .fixedSize(horizontal: false, vertical: true)
    }
}

struct Eyebrow: View {
    let text: LocalizedStringKey

    init(_ text: LocalizedStringKey) {
        self.text = text
    }

    var body: some View {
        Text(text)
            .font(.caption.weight(.semibold))
            .textCase(.uppercase)
            .tracking(1.1)
            .foregroundStyle(MoodprintTheme.primary)
    }
}
