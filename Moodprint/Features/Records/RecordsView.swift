import SwiftUI

struct RecordsView: View {
    @EnvironmentObject private var appState: AppState

    var body: some View {
        NavigationStack {
            Group {
                if appState.moods.isEmpty {
                    ContentUnavailableView(
                        "아직 기록이 없어요",
                        systemImage: "leaf",
                        description: Text("기록하지 않은 날에도 불이익은 없어요.\n필요할 때 편하게 마음을 남겨주세요.")
                    )
                } else {
                    ScrollView {
                        LazyVStack(spacing: 14) {
                            ForEach(appState.moods) { mood in
                                MoodRecordCard(
                                    mood: mood,
                                    result: result(for: mood),
                                    action: action(for: mood)
                                )
                            }
                        }
                        .padding(20)
                    }
                }
            }
            .background(MoodprintTheme.background)
            .navigationTitle("나의 마음 기록")
        }
    }

    private func result(for mood: MoodRecord) -> ActionResultRecord? {
        appState.results.first(where: { $0.moodID == mood.id })
    }

    private func action(for mood: MoodRecord) -> RecoveryActionRecord? {
        guard let result = result(for: mood) else { return nil }
        return appState.actions.first(where: { $0.id == result.actionID })
    }
}

private struct MoodRecordCard: View {
    let mood: MoodRecord
    let result: ActionResultRecord?
    let action: RecoveryActionRecord?

    var body: some View {
        VStack(alignment: .leading, spacing: 15) {
            HStack(alignment: .firstTextBaseline) {
                Text(mood.createdAt, format: .dateTime.month().day().weekday())
                    .font(.headline)
                Spacer()
                Text(mood.createdAt, format: .dateTime.hour().minute())
                    .font(.caption.monospacedDigit())
                    .foregroundStyle(MoodprintTheme.secondaryText)
            }

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 7) {
                    ForEach(mood.emotions) { emotion in
                        MoodprintTag(
                            text: LocalizedStringKey(emotion.rawValue),
                            color: MoodprintTheme.softPurple
                        )
                    }
                    MoodprintTag(
                        text: "에너지 \(mood.energy.rawValue)",
                        color: energyColor,
                        systemImage: "bolt.fill"
                    )
                }
            }

            if let note = mood.note {
                Text("“\(note)”")
                    .font(.body)
                    .foregroundStyle(MoodprintTheme.ink)
                    .fixedSize(horizontal: false, vertical: true)
                    .accessibilityLabel("남긴 글, \(note)")
            }

            Divider()

            if let result, let action {
                VStack(alignment: .leading, spacing: 10) {
                    RecordDetailRow(
                        eyebrow: "실행한 행동",
                        systemImage: action.symbol,
                        title: action.title
                    )
                    if let detail = result.detailNote {
                        Label(detail, systemImage: "music.note")
                            .font(.subheadline)
                            .foregroundStyle(MoodprintTheme.secondaryText)
                            .padding(.leading, 46)
                            .accessibilityLabel("남긴 행동 기록, \(detail)")
                    }
                    if let change = result.change {
                        RecordDetailRow(
                            eyebrow: "행동 후 변화",
                            systemImage: change.symbol,
                            title: change.rawValue
                        )
                    } else {
                        Label("행동을 완료했어요", systemImage: "checkmark.circle.fill")
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(MoodprintTheme.primary)
                    }
                }
            } else {
                Label("마음만 기록했어요", systemImage: "heart.text.clipboard")
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(MoodprintTheme.secondaryText)
            }
        }
        .moodprintCard()
        .accessibilityElement(children: .contain)
    }

    private var energyColor: Color {
        switch mood.energy {
        case .low: MoodprintTheme.mint
        case .medium: MoodprintTheme.warm
        case .high: MoodprintTheme.coral
        }
    }
}

private struct RecordDetailRow: View {
    let eyebrow: String
    let systemImage: String
    let title: String

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: systemImage)
                .font(.headline)
                .foregroundStyle(MoodprintTheme.primary)
                .frame(width: 34, height: 34)
                .background(MoodprintTheme.softPurple, in: Circle())
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 2) {
                Text(eyebrow)
                    .font(.caption)
                    .foregroundStyle(MoodprintTheme.secondaryText)
                Text(title)
                    .font(.subheadline.weight(.semibold))
            }
        }
    }
}

private extension MoodChange {
    var symbol: String {
        switch self {
        case .harder: "cloud.rain"
        case .same: "equal.circle"
        case .better: "sun.min.fill"
        case .muchBetter: "sun.max.fill"
        }
    }
}
