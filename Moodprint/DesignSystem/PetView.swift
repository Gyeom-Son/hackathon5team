import SwiftUI

enum PetMood {
    case calm
    case happy
}

struct PetView: View {
    let size: CGFloat
    let mood: PetMood
    var tint: Color = MoodprintTheme.petPurple
    var name: String = "몽실이"

    var body: some View {
        ZStack {
            Ellipse()
                .fill(tint.opacity(0.18))
                .frame(width: size * 0.84, height: size * 0.15)
                .offset(y: size * 0.42)
                .accessibilityHidden(true)

            MoodprintBlobShape()
                .fill(tint)
                .frame(width: size, height: size * 0.84)
                .overlay(eyes)
                .overlay(mouth)
                .accessibilityHidden(true)
        }
        .frame(width: size, height: size)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text("마음 동반자 \(name)"))
        .accessibilityValue(mood == .happy ? Text("기쁜 표정") : Text("편안한 표정"))
    }

    private var eyes: some View {
        HStack(spacing: size * 0.25) {
            Circle()
                .fill(MoodprintTheme.ink)
                .frame(width: size * 0.065, height: size * 0.065)
            Circle()
                .fill(MoodprintTheme.ink)
                .frame(width: size * 0.065, height: size * 0.065)
        }
        .offset(y: -size * 0.02)
    }

    @ViewBuilder
    private var mouth: some View {
        if mood == .happy {
            Capsule()
                .trim(from: 0.5, to: 1)
                .stroke(MoodprintTheme.ink, lineWidth: max(2, size * 0.014))
                .frame(width: size * 0.14, height: size * 0.1)
                .offset(y: size * 0.12)
        } else {
            Capsule()
                .fill(MoodprintTheme.ink)
                .frame(width: size * 0.11, height: max(2, size * 0.014))
                .offset(y: size * 0.12)
        }
    }
}

private struct MoodprintBlobShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.width * 0.5, y: rect.height * 0.03))
        path.addCurve(
            to: CGPoint(x: rect.width * 0.98, y: rect.height * 0.5),
            control1: CGPoint(x: rect.width * 0.82, y: 0),
            control2: CGPoint(x: rect.width, y: rect.height * 0.2)
        )
        path.addCurve(
            to: CGPoint(x: rect.width * 0.5, y: rect.height * 0.98),
            control1: CGPoint(x: rect.width, y: rect.height * 0.82),
            control2: CGPoint(x: rect.width * 0.8, y: rect.height)
        )
        path.addCurve(
            to: CGPoint(x: rect.width * 0.02, y: rect.height * 0.5),
            control1: CGPoint(x: rect.width * 0.2, y: rect.height),
            control2: CGPoint(x: 0, y: rect.height * 0.82)
        )
        path.addCurve(
            to: CGPoint(x: rect.width * 0.5, y: rect.height * 0.03),
            control1: CGPoint(x: 0, y: rect.height * 0.2),
            control2: CGPoint(x: rect.width * 0.18, y: 0)
        )
        return path
    }
}
