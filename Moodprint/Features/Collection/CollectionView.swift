import SwiftUI

struct CollectionView: View {
    @EnvironmentObject private var appState: AppState

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Eyebrow("마음 생물 도감")
                            Text("발견한 친구들")
                                .font(.title.bold())
                        }
                        Spacer()
                        MoodprintTag(
                            text: "\(appState.pets.filter(\.isUnlocked).count) / \(appState.pets.count)",
                            color: MoodprintTheme.softPurple
                        )
                    }

                    LazyVGrid(
                        columns: [GridItem(.flexible()), GridItem(.flexible())],
                        spacing: 12
                    ) {
                        ForEach(appState.pets) { pet in
                            VStack(spacing: 9) {
                                PetView(
                                    size: 78,
                                    mood: .calm,
                                    tint: pet.color,
                                    name: pet.isUnlocked ? pet.name : "아직 만나지 못한 친구"
                                )
                                .grayscale(pet.isUnlocked ? 0 : 1)
                                .opacity(pet.isUnlocked ? 1 : 0.45)
                                Text(pet.isUnlocked ? pet.name : "아직 만나지 못한 친구")
                                    .font(.headline)
                                    .multilineTextAlignment(.center)
                                Text(
                                    pet.isUnlocked
                                        ? "성장 \(pet.level)단계"
                                        : "조각 \(pet.fragments)/\(pet.requiredFragments)"
                                )
                                .font(.caption)
                                .foregroundStyle(MoodprintTheme.secondaryText)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(
                                MoodprintTheme.surface,
                                in: RoundedRectangle(cornerRadius: 18)
                            )
                        }
                    }

                    VStack(alignment: .leading, spacing: 6) {
                        Eyebrow("천천히 발견해요")
                        Text("기록하지 않은 날에도 불이익은 없어요. 작은 행동을 완료할 때 새로운 친구를 만날 수 있어요.")
                    }
                    .moodprintCard(background: MoodprintTheme.mint)
                }
                .padding(20)
            }
            .background(MoodprintTheme.background)
            .navigationTitle("도감")
        }
    }
}

private extension PetProgressRecord {
    var color: Color {
        switch colorName {
        case "mint": MoodprintTheme.petMint
        case "coral": MoodprintTheme.petCoral
        case "yellow": MoodprintTheme.petYellow
        default: MoodprintTheme.petPurple
        }
    }
}
