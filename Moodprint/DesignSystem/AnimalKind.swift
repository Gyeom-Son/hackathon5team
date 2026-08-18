import SwiftUI

/// 몽실이 스타일 참고 디자인(16종 동물 성장 3단계)에서 가져온 캐릭터 종류입니다.
/// `PetProgressRecord.colorName`에는 이 enum의 `rawValue`가 저장됩니다.
enum AnimalKind: String, CaseIterable, Codable {
    case cat, dog, rabbit, bear, fox, panda, lion, tiger
    case koala, squirrel, penguin, owl, sheep, pig, deer, chick

    /// 참고 디자인과 동일한 한글 동물 이름. `PetProgressRecord.name`으로도 사용됩니다.
    var koreanName: String {
        switch self {
        case .cat: "고양이"
        case .dog: "강아지"
        case .rabbit: "토끼"
        case .bear: "곰"
        case .fox: "여우"
        case .panda: "판다"
        case .lion: "사자"
        case .tiger: "호랑이"
        case .koala: "코알라"
        case .squirrel: "다람쥐"
        case .penguin: "펭귄"
        case .owl: "부엉이"
        case .sheep: "양"
        case .pig: "돼지"
        case .deer: "사슴"
        case .chick: "병아리"
        }
    }

    /// 참고 HTML(mongsili_animal_growth_stages_2.html)의 동물별 몸통 색과 동일한 값입니다.
    var bodyColor: Color {
        switch self {
        case .cat: Color(hex: 0xF0B98C)
        case .dog: Color(hex: 0xE0C49A)
        case .rabbit: Color(hex: 0xF6DDE7)
        case .bear: Color(hex: 0xC9A27A)
        case .fox: Color(hex: 0xF0935A)
        case .panda: Color(hex: 0xF5F5F5)
        case .lion: Color(hex: 0xE8A33D)
        case .tiger: Color(hex: 0xF0A24A)
        case .koala: Color(hex: 0xB9B6C4)
        case .squirrel: Color(hex: 0xC98A52)
        case .penguin: Color(hex: 0x3A3A44)
        case .owl: Color(hex: 0xA98A5C)
        case .sheep: Color(hex: 0xEFE9DA)
        case .pig: Color(hex: 0xF4B8C6)
        case .deer: Color(hex: 0xD3A878)
        case .chick: Color(hex: 0xF6D548)
        }
    }

    /// `PetProgressRecord.colorName`에 저장할 값. rawValue와 동일하게 유지합니다.
    var storageKey: String { rawValue }

    init(storageKey: String) {
        self = AnimalKind(rawValue: storageKey) ?? .cat
    }
}

extension PetProgressRecord {
    /// colorName에 저장된 값을 캐릭터 종류로 변환합니다.
    var animal: AnimalKind { AnimalKind(storageKey: colorName) }

    /// 1~3단계 성장 표현으로 정규화한 값입니다(레벨이 계속 올라가도 외형은 3단계에서 고정).
    var growthStage: Int { min(max(level, 1), 3) }
}

extension String {
    /// 마지막 글자의 받침 유무에 따라 "이"/"가" 주격 조사를 붙입니다. (예: "고양이" → "고양이가", "곰" → "곰이")
    var withSubjectParticle: String {
        guard let last = unicodeScalars.last, (0xAC00...0xD7A3).contains(last.value) else { return self + "가" }
        let hasBatchim = (last.value - 0xAC00) % 28 != 0
        return self + (hasBatchim ? "이" : "가")
    }
}

extension Color {
    init(hex: UInt32) {
        self.init(
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255
        )
    }
}
