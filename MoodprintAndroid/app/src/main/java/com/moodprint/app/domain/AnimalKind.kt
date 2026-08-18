package com.moodprint.app.domain

import androidx.compose.ui.graphics.Color

/**
 * 몽실이 스타일 참고 디자인(16종 동물 성장 3단계)에서 가져온 캐릭터 종류입니다.
 * [com.moodprint.app.data.local.PetProgressEntity.colorName]에는 [storageKey]가 저장됩니다.
 */
enum class AnimalKind(val storageKey: String, val koreanName: String, val bodyColor: Color) {
    CAT("cat", "고양이", Color(0xFFF0B98C)),
    DOG("dog", "강아지", Color(0xFFE0C49A)),
    RABBIT("rabbit", "토끼", Color(0xFFF6DDE7)),
    BEAR("bear", "곰", Color(0xFFC9A27A)),
    FOX("fox", "여우", Color(0xFFF0935A)),
    PANDA("panda", "판다", Color(0xFFF5F5F5)),
    LION("lion", "사자", Color(0xFFE8A33D)),
    TIGER("tiger", "호랑이", Color(0xFFF0A24A)),
    KOALA("koala", "코알라", Color(0xFFB9B6C4)),
    SQUIRREL("squirrel", "다람쥐", Color(0xFFC98A52)),
    PENGUIN("penguin", "펭귄", Color(0xFF3A3A44)),
    OWL("owl", "부엉이", Color(0xFFA98A5C)),
    SHEEP("sheep", "양", Color(0xFFEFE9DA)),
    PIG("pig", "돼지", Color(0xFFF4B8C6)),
    DEER("deer", "사슴", Color(0xFFD3A878)),
    CHICK("chick", "병아리", Color(0xFFF6D548));

    companion object {
        /** [colorName]에 저장된 값을 캐릭터 종류로 변환한다. 알 수 없는 값이면 고양이로 대체한다. */
        fun fromStorageKey(key: String?): AnimalKind =
            entries.firstOrNull { it.storageKey == key?.lowercase() } ?: CAT
    }
}

/** 1~3단계 성장 표현으로 정규화한다(레벨이 계속 올라가도 외형은 3단계에서 고정). */
fun growthStageFor(level: Int): Int = level.coerceIn(1, 3)

/** 마지막 글자의 받침 유무에 따라 "이"/"가" 주격 조사를 붙인다. (예: "고양이" → "고양이가", "곰" → "곰이") */
fun String.withSubjectParticle(): String {
    val last = lastOrNull() ?: return this + "가"
    if (last.code !in 0xAC00..0xD7A3) return this + "가"
    val hasBatchim = (last.code - 0xAC00) % 28 != 0
    return this + if (hasBatchim) "이" else "가"
}
