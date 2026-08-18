import SwiftUI

struct CollectionView: View {
    @EnvironmentObject private var appState: AppState
    @State private var selectedPet: PetProgressRecord?
    // 16종 전체 성장 단계를 한 번에 볼 수 있다. 확인이 끝나면 이 상태, 아래
    // 텍스트 버튼과 .sheet(showAnimalGallery) 블록, 그리고
    // Features/Debug/AnimalGalleryView.swift 파일을 지우면 깔끔하게 제거할 수 있다.
    @State private var showAnimalGallery = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Eyebrow("마음 생물 도감")
                            HStack(spacing: 16) {
                                Text("발견한 친구들")
                                    .font(.title.bold())
                                // 진입점 (제거 가능, 위 showAnimalGallery 선언부 주석 참고).
                                Button("전체 캐릭터 보기") {
                                    showAnimalGallery = true
                                }
                                .font(.caption)
                                .foregroundStyle(MoodprintTheme.primary)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .background(MoodprintTheme.softPurple, in: RoundedRectangle(cornerRadius: 6))
                            }
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
                                    animal: pet.animal,
                                    stage: pet.growthStage,
                                    name: pet.name,
                                    unlocked: pet.isUnlocked
                                )
                                Text(pet.isUnlocked ? pet.name : "아직 만나지 못한 친구")
                                    .font(.headline)
                                    .multilineTextAlignment(.center)
                                Text(
                                    pet.isUnlocked
                                        // 갓 대표로 설정된 동물은 레벨이 아직 0일 수 있는데, 그대로 쓰면
                                        // "0단계"로 보여 이상해진다. 카드 캐릭터 그림(growthStage)과
                                        // 같은 최소 1단계 기준으로 맞춘다.
                                        ? (pet.isPrimary ? "지금 대표 · \(pet.growthStage)단계" : "성장 \(pet.growthStage)단계")
                                        : "조각 \(pet.fragments)/\(pet.requiredFragments)"
                                )
                                .font(.caption)
                                .foregroundStyle(pet.isPrimary ? MoodprintTheme.primary : MoodprintTheme.secondaryText)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(
                                MoodprintTheme.surface,
                                in: RoundedRectangle(cornerRadius: 18)
                            )
                            .overlay(
                                RoundedRectangle(cornerRadius: 18)
                                    .stroke(pet.isPrimary ? MoodprintTheme.primary : .clear, lineWidth: 2)
                            )
                            .contentShape(RoundedRectangle(cornerRadius: 18))
                            .onTapGesture {
                                if pet.isUnlocked && !pet.isPrimary { selectedPet = pet }
                            }
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
            // 진입점 (제거 가능, 위 showAnimalGallery 선언부 주석 참고).
            .sheet(isPresented: $showAnimalGallery) {
                AnimalGalleryView()
            }
            .confirmationDialog(
                selectedPet?.name ?? "",
                isPresented: Binding(
                    get: { selectedPet != nil },
                    set: { if !$0 { selectedPet = nil } }
                ),
                titleVisibility: .visible
            ) {
                if let pet = selectedPet {
                    Button("\(pet.name) 대표로 설정") {
                        appState.setPrimaryPet(pet)
                        selectedPet = nil
                    }
                    Button("취소", role: .cancel) { selectedPet = nil }
                }
            } message: {
                Text("홈 화면에 표시되는 동반자를 바꿔요. 성장치는 각 친구별로 그대로 유지돼요.")
            }
        }
    }
}
