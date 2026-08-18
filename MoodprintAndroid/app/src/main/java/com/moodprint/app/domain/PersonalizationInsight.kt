package com.moodprint.app.domain

import com.moodprint.app.util.KoreanParticle

/** 한 번의 행동 완료 결과를, 그때 선택했던 감정들과 함께 묶어 개인화 인사이트 계산에 쓴다. */
data class MoodOutcome(
    val emotions: List<MoodEmotion>,
    val actionId: String,
    val change: MoodChange?,
)

data class PersonalizationInsight(
    val emotion: MoodEmotion,
    val actionTitle: String,
    val positiveCount: Int,
) {
    /** "OO님은 무기력을 느낄 때 가볍게 몸 풀기를 하면 기분이 나아지곤 했어요." 형태의 문구를 만든다. */
    fun message(nickname: String): String {
        val name = nickname.ifBlank { "마음 여행자" }
        val emotionPhrase = KoreanParticle.withObjectParticle(emotion.label)
        val actionPhrase = KoreanParticle.withObjectParticle(actionTitle)
        return "${name}님은 ${emotionPhrase} 느낄 때 ${actionPhrase} 하면 기분이 나아지곤 했어요."
    }
}

/**
 * 사용자가 실제로 남긴 기록에서만 개인화 문구를 만든다.
 * 데이터가 충분하지 않으면(같은 감정+행동 조합에서 긍정적 변화가 [MIN_POSITIVE_COUNT]번 미만이면)
 * null을 반환해, 확정되지 않은 내용을 확정된 것처럼 보여주지 않는다.
 */
object PersonalizationInsightService {
    private const val MIN_POSITIVE_COUNT = 2

    fun bestInsight(
        outcomes: List<MoodOutcome>,
        actions: List<RecoveryAction> = ActionCatalog.actions,
    ): PersonalizationInsight? {
        val positiveCounts = outcomes
            .filter { it.change == MoodChange.BETTER || it.change == MoodChange.MUCH_BETTER }
            .flatMap { outcome -> outcome.emotions.map { emotion -> EmotionActionKey(emotion, outcome.actionId) } }
            .groupingBy { it }
            .eachCount()

        val catalogOrder = actions.associate { it.id to it.catalogOrder }

        val best = positiveCounts.entries
            .filter { it.value >= MIN_POSITIVE_COUNT }
            .sortedWith(
                compareByDescending<Map.Entry<EmotionActionKey, Int>> { it.value }
                    .thenBy { catalogOrder[it.key.actionId] ?: Int.MAX_VALUE }
                    .thenBy { it.key.emotion.ordinal }
            )
            .firstOrNull() ?: return null

        val actionTitle = actions.firstOrNull { it.id == best.key.actionId }?.title ?: return null
        return PersonalizationInsight(
            emotion = best.key.emotion,
            actionTitle = actionTitle,
            positiveCount = best.value,
        )
    }

    private data class EmotionActionKey(val emotion: MoodEmotion, val actionId: String)
}
