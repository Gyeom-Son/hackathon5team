import SwiftUI

struct CheckInView: View {
    @EnvironmentObject private var appState: AppState
    @State private var selectedEmotions: [MoodEmotion] = []
    @State private var note = ""
    @State private var energy: MoodEnergy = .medium
    @State private var showNoteHelp = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    VStack(alignment: .leading, spacing: 5) {
                        Eyebrow("마음 기록")
                        Text("지금 마음을 남겨주세요")
                            .font(.title.bold())
                    }

                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            Text("감정 선택 · 최대 3개")
                                .font(.subheadline.weight(.semibold))
                            Spacer()
                            Text("\(selectedEmotions.count)/3")
                                .font(.caption.monospacedDigit())
                                .foregroundStyle(MoodprintTheme.secondaryText)
                        }
                        LazyVGrid(
                            columns: Array(repeating: GridItem(.flexible()), count: 3),
                            spacing: 10
                        ) {
                            ForEach(MoodEmotion.allCases) { emotion in
                                let selected = selectedEmotions.contains(emotion)
                                ChoiceChip(
                                    LocalizedStringKey(emotion.rawValue),
                                    isSelected: selected,
                                    isEnabled: selected || selectedEmotions.count < 3
                                ) {
                                    toggle(emotion)
                                }
                                .accessibilityIdentifier("emotion.\(emotion.rawValue)")
                            }
                        }
                    }

                    VStack(alignment: .leading, spacing: 10) {
                        HStack(spacing: 6) {
                            Text("무슨 일이 있었나요?")
                                .font(.subheadline.weight(.semibold))
                            Button {
                                showNoteHelp.toggle()
                            } label: {
                                Image(systemName: "info.circle")
                                    .foregroundStyle(MoodprintTheme.primary)
                                    .frame(
                                        minWidth: MoodprintTheme.minimumTouchTarget,
                                        minHeight: MoodprintTheme.minimumTouchTarget
                                    )
                                    .contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel("상황 기록 안내")
                            .popover(isPresented: $showNoteHelp, arrowEdge: .bottom) {
                                VStack(alignment: .leading, spacing: 8) {
                                    Text("꼭 적지 않아도 괜찮아요")
                                        .font(.headline)
                                    Text("상황을 함께 남기면 나중에 어떤 행동이 나에게 잘 맞았는지 돌아보기 쉬워져요.")
                                        .font(.subheadline)
                                        .foregroundStyle(MoodprintTheme.secondaryText)
                                }
                                .padding(18)
                                .frame(idealWidth: 300)
                                .presentationCompactAdaptation(.popover)
                            }
                        }
                        TextField(
                            "자유롭게 적어보세요.",
                            text: $note,
                            axis: .vertical
                        )
                        .lineLimit(4...8)
                        .padding(14)
                        .background(
                            MoodprintTheme.surface,
                            in: RoundedRectangle(cornerRadius: 14)
                        )
                        .overlay {
                            RoundedRectangle(cornerRadius: 14)
                                .stroke(MoodprintTheme.border)
                        }
                    }

                    VStack(alignment: .leading, spacing: 10) {
                        Text("현재 에너지")
                            .font(.subheadline.weight(.semibold))
                        Picker("현재 에너지", selection: $energy) {
                            ForEach(MoodEnergy.allCases) { level in
                                Text(level.rawValue).tag(level)
                            }
                        }
                        .pickerStyle(.segmented)
                    }

                    PrimaryButton("행동 추천받기") {
                        appState.saveMood(
                            emotions: selectedEmotions,
                            note: note,
                            energy: energy
                        )
                    }
                    .disabled(selectedEmotions.isEmpty)
                    .accessibilityIdentifier("checkin.submit")
                }
                .padding(20)
            }
            .scrollDismissesKeyboard(.interactively)
            .background(MoodprintTheme.background)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("뒤로", systemImage: "chevron.left") {
                        appState.route = .main
                    }
                }
            }
        }
    }

    private func toggle(_ emotion: MoodEmotion) {
        if let index = selectedEmotions.firstIndex(of: emotion) {
            selectedEmotions.remove(at: index)
        } else if selectedEmotions.count < 3 {
            selectedEmotions.append(emotion)
        }
    }
}
