import SwiftUI

struct HomeView: View {
    @EnvironmentObject private var appState: AppState
    @State private var showProfile = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    HStack(alignment: .top) {
                        VStack(alignment: .leading, spacing: 4) {
                            Eyebrow("오늘의 동반자")
                            // 갓 대표로 설정된 동물은 레벨이 아직 0일 수 있는데, 그대로 쓰면
                            // "0단계"로 보여 이상해진다. 도감 카드와 같은 최소 1단계 기준으로 맞춘다.
                            Text("\(appState.primaryPet?.name ?? AnimalKind.cat.koreanName) · \(appState.primaryPet?.growthStage ?? 1)단계")
                                .font(.headline)
                        }
                        Spacer()
                        MoodprintTag(
                            text: "도감 \(appState.pets.filter(\.isUnlocked).count)/\(appState.pets.count)",
                            color: MoodprintTheme.softPurple
                        )
                    }

                    VStack(spacing: 12) {
                        PetView(
                            size: 130,
                            mood: .calm,
                            animal: appState.primaryPet?.animal ?? .cat,
                            stage: appState.primaryPet?.growthStage ?? 1,
                            name: appState.primaryPet?.name ?? AnimalKind.cat.koreanName
                        )
                        HStack {
                            Text("다음 성장까지")
                            Spacer()
                            Text("\((appState.primaryPet?.experience ?? 0) % 100)%")
                        }
                        .font(.caption)
                        .foregroundStyle(MoodprintTheme.secondaryText)
                        ProgressView(
                            value: Double((appState.primaryPet?.experience ?? 0) % 100),
                            total: 100
                        )
                        .tint(MoodprintTheme.primary)
                    }
                    .moodprintCard(background: MoodprintTheme.softPurple)

                    Text("오늘 마음은 어때요?")
                        .font(.title.bold())
                    PrimaryButton("지금 마음 기록하기") {
                        appState.route = .checkIn
                    }
                    .accessibilityIdentifier("home.checkin")

                    if let latest = appState.moods.first {
                        VStack(alignment: .leading, spacing: 7) {
                            Eyebrow("최근 기록")
                            Text("\(latest.emotions.map(\.rawValue).joined(separator: " · ")) · 에너지 \(latest.energy.rawValue)")
                        }
                        .moodprintCard(background: MoodprintTheme.mint)
                    } else {
                        VStack(alignment: .leading, spacing: 7) {
                            Eyebrow("처음이라도 괜찮아요")
                            Text("기록하지 않은 날에도 불이익은 없어요. 필요할 때 시작해 주세요.")
                        }
                        .moodprintCard(background: MoodprintTheme.mint)
                    }
                }
                .padding(20)
            }
            .background(MoodprintTheme.background)
            .navigationTitle("Moodprint")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showProfile = true
                    } label: {
                        Image(systemName: "person.crop.circle")
                    }
                    .accessibilityLabel("프로필과 저장 안내")
                }
            }
            .sheet(isPresented: $showProfile) {
                LocalProfileSheet()
                    .presentationDetents([.medium])
                    .presentationDragIndicator(.visible)
            }
        }
    }
}

private struct LocalProfileSheet: View {
    @EnvironmentObject private var appState: AppState
    @Environment(\.dismiss) private var dismiss
    @State private var nickname = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("프로필") {
                    TextField("닉네임", text: $nickname)
                        .onSubmit { appState.updateNickname(nickname) }
                    Text("로그인 없이 사용 중")
                        .foregroundStyle(MoodprintTheme.secondaryText)
                }
                Section("기록 저장") {
                    Label("모든 기록은 이 기기에만 저장돼요.", systemImage: "iphone")
                    Label("앱을 삭제하면 기록도 함께 삭제돼요.", systemImage: "trash")
                        .foregroundStyle(MoodprintTheme.secondaryText)
                }
            }
            .navigationTitle("\(appState.nickname)님의 Moodprint")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("완료") {
                        appState.updateNickname(nickname)
                        dismiss()
                    }
                }
            }
            .onAppear { nickname = appState.nickname }
        }
    }
}
