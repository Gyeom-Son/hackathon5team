import Foundation

enum ActionCatalogSeed {
    static var actions: [RecoveryActionRecord] {
        [
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D01")!,
                title: "좋아하는 음악 한 곡 듣기",
                instruction: "다른 일을 잠시 멈추고 음악 한 곡을 끝까지 들어보세요.",
                durationSeconds: 180,
                category: .sensory,
                symbol: "headphones",
                supportedEmotions: [.anxious, .upset, .lonely, .complicated],
                supportedEnergies: [.low, .medium, .high],
                catalogOrder: 0,
                detailPrompt: "어떤 음악을 들었나요?",
                detailPlaceholder: "곡이나 아티스트를 적어보세요"
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D02")!,
                title: "창문 열고 천천히 숨 쉬기",
                instruction: "편한 자세에서 바깥 공기를 느끼며 천천히 숨을 쉬어보세요.",
                durationSeconds: 120,
                category: .rest,
                symbol: "wind",
                supportedEmotions: [.anxious, .angry, .complicated],
                supportedEnergies: [.low, .medium],
                catalogOrder: 1
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D03")!,
                title: "가볍게 몸 풀기",
                instruction: "어깨와 목부터 무리하지 않는 범위에서 가볍게 움직여보세요.",
                durationSeconds: 180,
                category: .movement,
                symbol: "figure.cooldown",
                supportedEmotions: [.lethargic, .angry, .upset],
                supportedEnergies: [.medium, .high],
                catalogOrder: 2
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D04")!,
                title: "지금 떠오르는 말 적기",
                instruction: "정리하려 애쓰지 말고 지금 떠오르는 말을 짧게 적어보세요.",
                durationSeconds: 180,
                category: .expression,
                symbol: "pencil.line",
                supportedEmotions: [.upset, .angry, .lonely, .complicated],
                supportedEnergies: [.low, .medium, .high],
                catalogOrder: 3
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D05")!,
                title: "따뜻한 물 한 잔 마시기",
                instruction: "물을 천천히 마시며 온도와 감각에 잠시 집중해보세요.",
                durationSeconds: 120,
                category: .sensory,
                symbol: "mug",
                supportedEmotions: [.lethargic, .upset, .lonely],
                supportedEnergies: [.low, .medium],
                catalogOrder: 4
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D06")!,
                title: "좋아하는 향 맡아보기",
                instruction: "차나 비누처럼 편하게 느껴지는 향에 잠시 집중해보세요.",
                durationSeconds: 60,
                category: .sensory,
                symbol: "nose",
                supportedEmotions: [.anxious, .complicated],
                supportedEnergies: [.low, .medium, .high],
                catalogOrder: 5,
                detailPrompt: "어떤 향이었나요?",
                detailPlaceholder: "기억하고 싶은 향을 적어보세요"
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D07")!,
                title: "차가운 물로 손 씻기",
                instruction: "차가운 물의 온도와 손끝의 감각을 천천히 느껴보세요.",
                durationSeconds: 60,
                category: .sensory,
                symbol: "drop.fill",
                supportedEmotions: [.angry, .complicated],
                supportedEnergies: [.medium, .high],
                catalogOrder: 6
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D08")!,
                title: "보라색 물건 3개 찾기",
                instruction: "주변을 천천히 살피며 보라색 물건 세 개를 찾아보세요.",
                durationSeconds: 120,
                category: .sensory,
                symbol: "eye.fill",
                supportedEmotions: [.anxious, .complicated],
                supportedEnergies: [.low, .medium, .high],
                catalogOrder: 7,
                detailPrompt: "무엇을 찾았나요?",
                detailPlaceholder: "찾은 물건을 적어보세요"
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D09")!,
                title: "눈을 감고 어깨 힘 풀기",
                instruction: "편하게 앉아 어깨에 들어간 힘을 천천히 풀어보세요.",
                durationSeconds: 120,
                category: .rest,
                symbol: "moon.zzz.fill",
                supportedEmotions: [.anxious, .upset],
                supportedEnergies: [.low],
                catalogOrder: 8
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D10")!,
                title: "휴대폰을 뒤집고 잠시 쉬기",
                instruction: "휴대폰을 내려놓고 화면 없는 시간을 잠시 가져보세요.",
                durationSeconds: 180,
                category: .rest,
                symbol: "iphone.slash",
                supportedEmotions: [.complicated, .lethargic],
                supportedEnergies: [.low, .medium],
                catalogOrder: 9
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D11")!,
                title: "조명을 낮추고 편하게 앉기",
                instruction: "조명을 조금 낮추고 가장 편한 자세로 잠시 앉아보세요.",
                durationSeconds: 180,
                category: .rest,
                symbol: "lightbulb.slash.fill",
                supportedEmotions: [.anxious, .lethargic],
                supportedEnergies: [.low],
                catalogOrder: 10
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D12")!,
                title: "지금 필요한 것 한 단어로 적기",
                instruction: "지금 나에게 필요한 것을 한 단어로 남겨보세요.",
                durationSeconds: 120,
                category: .expression,
                symbol: "text.cursor",
                supportedEmotions: [.complicated, .upset],
                supportedEnergies: [.low, .medium, .high],
                catalogOrder: 11,
                detailPrompt: "지금 필요한 것은 무엇인가요?",
                detailPlaceholder: "한 단어로 적어보세요"
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D13")!,
                title: "오늘 견뎌낸 일 하나 적기",
                instruction: "크고 작음을 따지지 말고 오늘 지나온 일 하나를 적어보세요.",
                durationSeconds: 120,
                category: .expression,
                symbol: "checkmark.seal.fill",
                supportedEmotions: [.lethargic, .upset],
                supportedEnergies: [.low, .medium],
                catalogOrder: 12,
                detailPrompt: "오늘 무엇을 견뎌냈나요?",
                detailPlaceholder: "짧게 남겨보세요"
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D14")!,
                title: "보내지 않을 메시지 작성하기",
                instruction: "보내지 않아도 괜찮으니 하고 싶었던 말을 적어보세요.",
                durationSeconds: 180,
                category: .expression,
                symbol: "envelope.open.fill",
                supportedEmotions: [.angry, .upset, .lonely],
                supportedEnergies: [.low, .medium, .high],
                catalogOrder: 13,
                detailPrompt: "어떤 말을 남기고 싶나요?",
                detailPlaceholder: "보내지 않을 말을 적어보세요"
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D15")!,
                title: "사진첩에서 좋아하는 사진 보기",
                instruction: "마음에 드는 사진 한 장을 골라 천천히 바라보세요.",
                durationSeconds: 120,
                category: .reflection,
                symbol: "photo.fill",
                supportedEmotions: [.lonely, .upset],
                supportedEnergies: [.low],
                catalogOrder: 14,
                detailPrompt: "어떤 사진이었나요?",
                detailPlaceholder: "사진에 대한 기억을 적어보세요"
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D16")!,
                title: "고마웠던 순간 하나 떠올리기",
                instruction: "최근에 작게라도 고마웠던 순간 하나를 떠올려보세요.",
                durationSeconds: 120,
                category: .reflection,
                symbol: "heart.fill",
                supportedEmotions: [.lonely, .lethargic],
                supportedEnergies: [.low, .medium],
                catalogOrder: 15,
                detailPrompt: "어떤 순간이 떠올랐나요?",
                detailPlaceholder: "고마웠던 순간을 적어보세요"
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D17")!,
                title: "자리에서 일어나 기지개 켜기",
                instruction: "무리하지 않는 범위에서 팔을 뻗고 천천히 기지개를 켜보세요.",
                durationSeconds: 60,
                category: .movement,
                symbol: "figure.stand",
                supportedEmotions: [.lethargic, .complicated],
                supportedEnergies: [.medium, .high],
                catalogOrder: 16
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D18")!,
                title: "방 안을 천천히 한 바퀴 걷기",
                instruction: "속도를 내지 말고 발바닥의 감각을 느끼며 걸어보세요.",
                durationSeconds: 120,
                category: .movement,
                symbol: "figure.walk",
                supportedEmotions: [.angry, .lethargic],
                supportedEnergies: [.medium, .high],
                catalogOrder: 17
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D19")!,
                title: "책상 위 물건 하나 정리하기",
                instruction: "눈에 보이는 작은 물건 하나만 제자리로 옮겨보세요.",
                durationSeconds: 180,
                category: .environment,
                symbol: "square.grid.3x3.fill",
                supportedEmotions: [.complicated, .lethargic],
                supportedEnergies: [.medium],
                catalogOrder: 18,
                detailPrompt: "무엇을 정리했나요?",
                detailPlaceholder: "정리한 물건을 적어보세요"
            ),
            RecoveryActionRecord(
                id: UUID(uuidString: "17E3A608-17F8-4BEA-94B3-370DFBF82D20")!,
                title: "창밖 풍경 사진 한 장 찍기",
                instruction: "마음에 들어오는 풍경을 찾아 사진 한 장으로 남겨보세요.",
                durationSeconds: 120,
                category: .environment,
                symbol: "camera.fill",
                supportedEmotions: [.lonely, .complicated],
                supportedEnergies: [.low, .medium, .high],
                catalogOrder: 19,
                detailPrompt: "어떤 풍경이었나요?",
                detailPlaceholder: "사진에 담은 풍경을 적어보세요"
            )
        ]
    }
}

/// 마음 생물 도감 16종의 고정 UUID. 참고 디자인(`mongsili_animal_growth_stages_2.html`)의
/// 동물 순서(고양이→강아지→토끼→곰→여우→판다→사자→호랑이→코알라→다람쥐→펭귄→부엉이→양→돼지→사슴→병아리)와 동일합니다.
enum StablePetIDs {
    static let ids: [AnimalKind: UUID] = [
        .cat: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421101")!,
        .dog: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421102")!,
        .rabbit: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421103")!,
        .bear: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421104")!,
        .fox: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421105")!,
        .panda: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421106")!,
        .lion: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421107")!,
        .tiger: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421108")!,
        .koala: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421109")!,
        .squirrel: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421110")!,
        .penguin: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421111")!,
        .owl: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421112")!,
        .sheep: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421113")!,
        .pig: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421114")!,
        .deer: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421115")!,
        .chick: UUID(uuidString: "4B0EE180-65EB-4703-89EA-F695DF421116")!,
    ]
}

enum PetCatalogSeed {
    /// 기본 동반자는 고양이입니다. 나머지 15종은 잠금 상태로 시작해 조각을 모으며 도감을 채웁니다.
    static let primaryAnimal: AnimalKind = .cat

    static var pets: [PetProgressRecord] {
        AnimalKind.allCases.map { animal in
            let isPrimary = animal == primaryAnimal
            return PetProgressRecord(
                id: StablePetIDs.ids[animal]!,
                name: animal.koreanName,
                colorName: animal.storageKey,
                level: isPrimary ? 1 : 0,
                experience: 0,
                fragments: isPrimary ? 3 : 0,
                requiredFragments: isPrimary ? 3 : 5,
                isUnlocked: isPrimary,
                isPrimary: isPrimary
            )
        }
    }
}
