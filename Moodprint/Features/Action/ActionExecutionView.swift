import SwiftUI

struct ActionExecutionView: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.scenePhase) private var scenePhase
    let action: RecoveryActionRecord

    @State private var secondsRemaining: Int
    @State private var endDate: Date?
    @State private var isRunning = true
    @State private var showChangeSheet = false
    @State private var actionDetail = ""

    init(action: RecoveryActionRecord) {
        self.action = action
        _secondsRemaining = State(initialValue: action.durationSeconds)
        _endDate = State(initialValue: Date().addingTimeInterval(Double(action.durationSeconds)))
    }

    var body: some View {
        NavigationStack {
            TimelineView(.periodic(from: .now, by: 1)) { _ in
                ScrollView {
                    VStack(spacing: 18) {
                        Eyebrow(LocalizedStringKey(action.title))
                        ZStack {
                            Circle().stroke(MoodprintTheme.softPurple, lineWidth: 10)
                            Circle()
                                .trim(
                                    from: 0,
                                    to: CGFloat(secondsRemaining) / CGFloat(max(action.durationSeconds, 1))
                                )
                                .stroke(
                                    MoodprintTheme.primary,
                                    style: StrokeStyle(lineWidth: 10, lineCap: .round)
                                )
                                .rotationEffect(.degrees(-90))
                            Text(timeText)
                                .font(.largeTitle.monospacedDigit().bold())
                        }
                        .frame(width: 190, height: 190)
                        .accessibilityElement(children: .ignore)
                        .accessibilityLabel("남은 시간")
                        .accessibilityValue(timeText)

                        Text("화면을 보지 않아도 괜찮아요.")
                            .font(.headline)
                        Text("행동이 끝나면 다시 만나요.")
                            .foregroundStyle(MoodprintTheme.secondaryText)
                        if let detailPrompt = action.detailPrompt {
                            VStack(alignment: .leading, spacing: 8) {
                                Label(detailPrompt, systemImage: action.symbol)
                                    .font(.subheadline.weight(.semibold))
                                TextField(
                                    "\(action.detailPlaceholder ?? "짧게 남겨보세요") · 선택",
                                    text: $actionDetail
                                )
                                    .padding(13)
                                    .background(
                                        MoodprintTheme.surface,
                                        in: RoundedRectangle(cornerRadius: 13)
                                    )
                                    .overlay {
                                        RoundedRectangle(cornerRadius: 13)
                                            .stroke(MoodprintTheme.border)
                                    }
                                    .accessibilityLabel("들은 음악, 선택 입력")
                            }
                            .moodprintCard(background: MoodprintTheme.mint, padding: 14)
                        }
                        PetView(
                            size: 100,
                            mood: .calm,
                            animal: appState.primaryPet?.animal ?? .cat,
                            stage: appState.primaryPet?.growthStage ?? 1,
                            name: appState.primaryPet?.name ?? AnimalKind.cat.koreanName
                        )
                    }
                    .frame(maxWidth: .infinity)
                    .padding(20)
                }
                .safeAreaInset(edge: .bottom) {
                    HStack(spacing: 12) {
                        SecondaryButton(isRunning ? "일시정지" : "계속하기") {
                            toggleTimer()
                        }
                        PrimaryButton("완료") {
                            pause()
                            showChangeSheet = true
                        }
                        .accessibilityIdentifier("action.complete")
                    }
                    .padding(20)
                    .background(.ultraThinMaterial)
                }
                .onChange(of: Date.now, initial: true) { _, _ in updateRemaining() }
            }
            .background(MoodprintTheme.background)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("뒤로", systemImage: "chevron.left") {
                        appState.route = .recommendation
                    }
                }
            }
            .onChange(of: scenePhase) { _, phase in
                if phase == .active { updateRemaining() }
            }
            .sheet(isPresented: $showChangeSheet) {
                ChangeSheet { change in
                    showChangeSheet = false
                    appState.finishCurrentAction(
                        change: change,
                        detailNote: actionDetail
                    )
                }
                .presentationDetents([.medium, .large])
                .presentationDragIndicator(.visible)
                .interactiveDismissDisabled()
            }
        }
    }

    private var timeText: String {
        String(format: "%02d:%02d", secondsRemaining / 60, secondsRemaining % 60)
    }

    private func updateRemaining() {
        guard isRunning, let endDate else { return }
        secondsRemaining = max(0, Int(ceil(endDate.timeIntervalSinceNow)))
        if secondsRemaining == 0 {
            isRunning = false
            self.endDate = nil
            showChangeSheet = true
        }
    }

    private func toggleTimer() {
        isRunning ? pause() : resume()
    }

    private func pause() {
        updateRemaining()
        isRunning = false
        endDate = nil
    }

    private func resume() {
        guard secondsRemaining > 0 else {
            showChangeSheet = true
            return
        }
        endDate = Date().addingTimeInterval(Double(secondsRemaining))
        isRunning = true
    }
}

private struct ChangeSheet: View {
    let onSave: (MoodChange?) -> Void
    @State private var selected: MoodChange?

    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            Eyebrow("행동 변화 기록")
            Text("행동 전보다 지금은 어떤가요?")
                .font(.title2.bold())
            Text("기분이 나아지지 않아도 괜찮고, 기록을 건너뛰어도 펫은 똑같이 성장해요.")
                .foregroundStyle(MoodprintTheme.secondaryText)
            LazyVGrid(
                columns: [GridItem(.flexible()), GridItem(.flexible())],
                spacing: 10
            ) {
                ForEach(MoodChange.allCases) { change in
                    ChoiceChip(
                        LocalizedStringKey(change.rawValue),
                        isSelected: selected == change
                    ) {
                        selected = change
                    }
                }
            }
            Spacer()
            PrimaryButton("변화 기록하기") { onSave(selected) }
            SecondaryButton("지금은 기록하지 않을래요") { onSave(nil) }
                .accessibilityIdentifier("change.skip")
        }
        .padding(22)
    }
}
