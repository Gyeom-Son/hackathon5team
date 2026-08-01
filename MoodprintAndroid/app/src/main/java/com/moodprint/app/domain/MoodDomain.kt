package com.moodprint.app.domain

enum class MoodEmotion(val label: String) {
    ANXIOUS("불안"),
    LETHARGIC("무기력"),
    UPSET("속상함"),
    ANGRY("화남"),
    LONELY("외로움"),
    COMPLICATED("복잡함"),
    TIRED("지침"),
    FRUSTRATED("답답함"),
    SAD("슬픔"),
}

enum class MoodEnergy(val label: String) {
    LOW("낮음"),
    MEDIUM("보통"),
    HIGH("높음"),
}

enum class MoodChange(val label: String, val recommendationWeight: Double) {
    HARDER("더 힘들어요", -0.25),
    SAME("비슷해요", 0.0),
    BETTER("조금 나아졌어요", 0.2),
    MUCH_BETTER("많이 나아졌어요", 0.35),
}

enum class RecoveryActionCategory(val label: String) {
    SENSORY("감각 행동"),
    MOVEMENT("움직임"),
    REST("휴식"),
    EXPRESSION("표현"),
    REFLECTION("회상"),
    ENVIRONMENT("환경"),
}

data class RecoveryAction(
    /** iOS SeedCatalog와 공유하는 변경되지 않는 UUID 문자열이다. */
    val id: String,
    val title: String,
    val instruction: String,
    val durationSeconds: Int,
    val category: RecoveryActionCategory,
    val symbolName: String,
    val supportedEmotions: Set<MoodEmotion>,
    val supportedEnergies: Set<MoodEnergy>,
    val catalogOrder: Int,
    val detailPrompt: String? = null,
    val detailPlaceholder: String? = null,
)

data class ActionHistory(
    val actionId: String,
    val change: MoodChange?,
    val completedAtEpochMillis: Long = 0,
)

data class ActionRecommendation(
    val action: RecoveryAction,
    val score: Double,
    val reason: String,
)
