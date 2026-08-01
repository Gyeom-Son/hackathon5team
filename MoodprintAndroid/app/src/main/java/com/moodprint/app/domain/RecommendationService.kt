package com.moodprint.app.domain

class RecommendationService {
    fun recommend(
        emotions: List<MoodEmotion>,
        energy: MoodEnergy,
        actions: List<RecoveryAction> = ActionCatalog.actions,
        history: List<ActionHistory> = emptyList(),
    ): List<ActionRecommendation> {
        if (emotions.size !in 1..3 || emotions.distinct().size != emotions.size) return emptyList()

        val selectedEmotions = emotions.toSet()
        val historyByAction = history.groupBy(ActionHistory::actionId)
        val recentActionIds = history
            .sortedByDescending(ActionHistory::completedAtEpochMillis)
            .take(3)
            .map(ActionHistory::actionId)

        return actions.map { action ->
            val actionHistory = historyByAction[action.id].orEmpty()
            val emotionMatchCount = selectedEmotions.intersect(action.supportedEmotions).size
            val energyMatches = energy in action.supportedEnergies
            val completedCount = actionHistory.size
            val recordedChanges = actionHistory.mapNotNull(ActionHistory::change)
            val averageChange = recordedChanges
                .map(MoodChange::recommendationWeight)
                .average()
                .takeUnless(Double::isNaN) ?: 0.0
            val recentIndex = recentActionIds.indexOf(action.id)
            val recentPenalty = when (recentIndex) {
                0 -> 9.0
                1 -> 6.0
                2 -> 3.0
                else -> 0.0
            }
            val score = emotionMatchCount * 10.0 +
                (if (energyMatches) 5.0 else -4.0) -
                completedCount.coerceAtMost(5) * 0.75 +
                averageChange * 3.0 -
                recentPenalty

            RankedRecommendation(
                recommendation = ActionRecommendation(
                    action = action,
                    score = score,
                    reason = reason(emotionMatchCount, energyMatches, energy, actionHistory),
                ),
                emotionMatchCount = emotionMatchCount,
                energyMatches = energyMatches,
                completedCount = completedCount,
            )
        }.sortedWith(
            compareByDescending<RankedRecommendation> { it.recommendation.score }
                .thenByDescending { it.emotionMatchCount }
                .thenByDescending { it.energyMatches }
                .thenByDescending { it.completedCount }
                .thenBy { it.recommendation.action.catalogOrder },
        ).map(RankedRecommendation::recommendation)
    }

    private fun reason(
        emotionMatchCount: Int,
        energyMatches: Boolean,
        energy: MoodEnergy,
        actionHistory: List<ActionHistory>,
    ): String {
        val energyText = when (energy) {
            MoodEnergy.LOW -> "낮은"
            MoodEnergy.MEDIUM -> "보통"
            MoodEnergy.HIGH -> "높은"
        }
        val inputText = when {
            emotionMatchCount > 0 && energyMatches -> "$energyText 에너지와 선택한 감정 ${emotionMatchCount}개를 고려해, 지금 부담 없이 할 수 있는 행동을 준비했어요."
            emotionMatchCount > 0 -> "선택한 감정 ${emotionMatchCount}개를 고려해, 지금 시도하기 좋은 행동을 준비했어요."
            energyMatches -> "$energyText 에너지에 맞춰, 지금 부담 없이 할 수 있는 행동을 준비했어요."
            else -> "지금 입력한 상태에서 비교적 부담이 적은 행동을 준비했어요."
        }

        // 이 행동에 대한 이력이 없는 사용자에게 과거 기록을 근거로 말하지 않는다.
        if (actionHistory.isEmpty()) return inputText

        val positiveCount = actionHistory.count {
            it.change == MoodChange.BETTER || it.change == MoodChange.MUCH_BETTER
        }
        return if (positiveCount > 0) {
            "$inputText 이전 기록에서는 ${positiveCount}번 도움이 되었다고 남겼어요."
        } else {
            "$inputText 이전에도 완료해 본 행동이에요."
        }
    }
}

private data class RankedRecommendation(
    val recommendation: ActionRecommendation,
    val emotionMatchCount: Int,
    val energyMatches: Boolean,
    val completedCount: Int,
)
