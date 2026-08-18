import SwiftUI

/// 16종 동물의 1·2·3단계 성장 모습을 한 번에 볼 수 있게 모아 보여주는 화면.
/// 도감 화면(`CollectionView`)의 "전체 캐릭터 보기" 버튼으로만 연결되어 있어서,
/// 확인이 끝나면 이 파일과 `CollectionView`의 해당 버튼 / `.sheet(isPresented: $showAnimalGallery)`
/// 블록만 지우면 깔끔하게 제거할 수 있다.
struct AnimalGalleryView: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 18) {
                    Text("실제 데이터와 무관하게 항상 해금된 상태로 보여줘요. 확인이 끝나면 닫기를 눌러주세요.")
                        .font(.caption)
                        .foregroundStyle(MoodprintTheme.secondaryText)

                    ForEach(AnimalKind.allCases, id: \.self) { animal in
                        VStack(alignment: .leading, spacing: 8) {
                            Text(animal.koreanName)
                                .font(.subheadline.bold())
                            HStack(spacing: 0) {
                                ForEach([1, 2, 3], id: \.self) { stage in
                                    VStack(spacing: 6) {
                                        PetView(
                                            size: 84,
                                            mood: .happy,
                                            animal: animal,
                                            stage: stage,
                                            name: animal.koreanName,
                                            unlocked: true
                                        )
                                        Text("\(stage)단계")
                                            .font(.caption2)
                                            .foregroundStyle(MoodprintTheme.secondaryText)
                                    }
                                    .frame(maxWidth: .infinity)
                                }
                            }
                        }
                        .padding(12)
                        .background(MoodprintTheme.surface, in: RoundedRectangle(cornerRadius: 16))
                    }
                }
                .padding(20)
            }
            .background(MoodprintTheme.background)
            .navigationTitle("캐릭터 전체 보기")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("닫기") { dismiss() }
                }
            }
        }
    }
}
