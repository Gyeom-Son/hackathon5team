package com.moodprint.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersonalizationInsightServiceTest {
    private val actionA = ActionCatalog.actions[0]
    private val actionB = ActionCatalog.actions[1]

    @Test fun returnsNullWithoutEnoughPositiveHistory() {
        val outcomes = listOf(
            MoodOutcome(listOf(MoodEmotion.ANXIOUS), actionA.id, MoodChange.BETTER),
        )

        assertNull(PersonalizationInsightService.bestInsight(outcomes))
    }

    @Test fun returnsNullWhenChangesAreNotPositive() {
        val outcomes = listOf(
            MoodOutcome(listOf(MoodEmotion.ANXIOUS), actionA.id, MoodChange.SAME),
            MoodOutcome(listOf(MoodEmotion.ANXIOUS), actionA.id, MoodChange.HARDER),
        )

        assertNull(PersonalizationInsightService.bestInsight(outcomes))
    }

    @Test fun findsMostFrequentPositiveEmotionActionPair() {
        val outcomes = listOf(
            MoodOutcome(listOf(MoodEmotion.ANXIOUS), actionA.id, MoodChange.BETTER),
            MoodOutcome(listOf(MoodEmotion.ANXIOUS), actionA.id, MoodChange.MUCH_BETTER),
            MoodOutcome(listOf(MoodEmotion.SAD), actionB.id, MoodChange.BETTER),
        )

        val insight = PersonalizationInsightService.bestInsight(outcomes)

        assertEquals(MoodEmotion.ANXIOUS, insight?.emotion)
        assertEquals(actionA.title, insight?.actionTitle)
        assertEquals(2, insight?.positiveCount)
    }

    @Test fun ignoresSessionsWithoutRecordedChange() {
        val outcomes = listOf(
            MoodOutcome(listOf(MoodEmotion.ANXIOUS), actionA.id, null),
            MoodOutcome(listOf(MoodEmotion.ANXIOUS), actionA.id, null),
        )

        assertNull(PersonalizationInsightService.bestInsight(outcomes))
    }

    @Test fun messageUsesCorrectKoreanParticles() {
        val insight = PersonalizationInsight(MoodEmotion.LETHARGIC, "가볍게 몸 풀기", 2)

        assertEquals(
            "혜진님은 무기력을 느낄 때 가볍게 몸 풀기를 하면 기분이 나아지곤 했어요.",
            insight.message("혜진"),
        )
    }

    @Test fun messageFallsBackToDefaultNicknameWhenBlank() {
        val insight = PersonalizationInsight(MoodEmotion.SAD, "따뜻한 물 한 잔 마시기", 2)

        assertEquals(
            "마음 여행자님은 슬픔을 느낄 때 따뜻한 물 한 잔 마시기를 하면 기분이 나아지곤 했어요.",
            insight.message(""),
        )
    }
}
