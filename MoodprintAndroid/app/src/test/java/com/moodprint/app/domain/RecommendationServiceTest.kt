package com.moodprint.app.domain

import org.junit.Assert.*
import org.junit.Test

class RecommendationServiceTest {
    private val service = RecommendationService()

    @Test fun rejectsZeroAndFourEmotions() {
        assertTrue(service.recommend(emptyList(), MoodEnergy.MEDIUM).isEmpty())
        assertTrue(service.recommend(listOf(MoodEmotion.ANXIOUS, MoodEmotion.SAD, MoodEmotion.TIRED, MoodEmotion.LONELY), MoodEnergy.MEDIUM).isEmpty())
    }

    @Test fun usesAllSelectedEmotionsAndEnergy() {
        val recommendations = service.recommend(
            listOf(MoodEmotion.ANXIOUS, MoodEmotion.COMPLICATED, MoodEmotion.ANGRY),
            MoodEnergy.LOW
        )
        assertTrue(recommendations.isNotEmpty())
        assertTrue(recommendations.first().score >= recommendations.last().score)
        assertTrue(recommendations.first().reason.contains("감정"))
        assertTrue(recommendations.first().reason.contains("에너지"))
    }

    @Test fun newUserReasonNeverClaimsPastRecords() {
        val reason = service.recommend(listOf(MoodEmotion.SAD), MoodEnergy.LOW).first().reason
        assertFalse(reason.contains("이전 기록"))
        assertFalse(reason.contains("이전에 완료"))
    }

    @Test fun positiveChangeScoresHigherThanSameCompletedHistory() {
        val target = ActionCatalog.actions.first()
        val same = service.recommend(
            listOf(MoodEmotion.ANXIOUS), MoodEnergy.MEDIUM,
            history = listOf(ActionHistory(target.id, MoodChange.SAME, 100))
        )
        val better = service.recommend(
            listOf(MoodEmotion.ANXIOUS), MoodEnergy.MEDIUM,
            history = listOf(ActionHistory(target.id, MoodChange.MUCH_BETTER, 100))
        )
        assertTrue(
            better.first { it.action.id == target.id }.score >
                same.first { it.action.id == target.id }.score
        )
    }

    @Test fun mostRecentlyCompletedActionIsPenalized() {
        val candidates = ActionCatalog.actions.take(2)
        val base = service.recommend(listOf(MoodEmotion.ANXIOUS), MoodEnergy.MEDIUM, candidates)
        val previousFirst = base.first().action
        val rotated = service.recommend(
            listOf(MoodEmotion.ANXIOUS), MoodEnergy.MEDIUM, candidates,
            history = listOf(ActionHistory(previousFirst.id, MoodChange.SAME, 200))
        )
        assertNotEquals(previousFirst.id, rotated.first().action.id)
    }

    @Test fun exactEnergyMatchBeatsMismatchWhenEmotionMatchIsEqual() {
        val low = ActionCatalog.actions[0].copy(
            supportedEmotions = setOf(MoodEmotion.ANGRY),
            supportedEnergies = setOf(MoodEnergy.LOW),
        )
        val highOnly = ActionCatalog.actions[1].copy(
            supportedEmotions = setOf(MoodEmotion.ANGRY),
            supportedEnergies = setOf(MoodEnergy.MEDIUM, MoodEnergy.HIGH),
        )
        val ranked = service.recommend(listOf(MoodEmotion.ANGRY), MoodEnergy.LOW, listOf(low, highOnly))
        assertEquals(low.id, ranked.first().action.id)
    }

    @Test fun reasonOnlyClaimsTheNumberOfEmotionsActuallyMatched() {
        val action = ActionCatalog.actions.first().copy(
            supportedEmotions = setOf(MoodEmotion.ANXIOUS),
            supportedEnergies = setOf(MoodEnergy.LOW),
        )
        val result = service.recommend(
            listOf(MoodEmotion.ANXIOUS, MoodEmotion.ANGRY, MoodEmotion.TIRED),
            MoodEnergy.LOW,
            actions = listOf(action),
        ).single()

        assertTrue(result.reason.contains("감정 1개"))
        assertFalse(result.reason.contains("감정 3개"))
    }

    @Test fun reasonDoesNotClaimEnergyWhenActionDoesNotSupportIt() {
        val action = ActionCatalog.actions.first().copy(
            supportedEmotions = setOf(MoodEmotion.ANXIOUS),
            supportedEnergies = setOf(MoodEnergy.HIGH),
        )
        val reason = service.recommend(
            listOf(MoodEmotion.ANXIOUS), MoodEnergy.LOW, listOf(action),
        ).single().reason

        assertFalse(reason.contains("선택한 에너지"))
    }

    @Test fun threeRecentCompletionsReceiveDescendingPenalties() {
        val candidates = ActionCatalog.actions.take(4)
        val history = listOf(
            ActionHistory(candidates[0].id, null, 400),
            ActionHistory(candidates[1].id, null, 300),
            ActionHistory(candidates[2].id, null, 200),
        )
        val ranked = service.recommend(
            listOf(MoodEmotion.ANXIOUS), MoodEnergy.MEDIUM, candidates, history,
        )
        val scores = ranked.associate { it.action.id to it.score }

        assertTrue(scores.getValue(candidates[0].id) < scores.getValue(candidates[1].id))
    }
}
