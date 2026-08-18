package com.moodprint.app.data

import org.junit.Assert.assertThrows
import org.junit.Test

class MoodInputValidatorTest {
    @Test
    fun acceptsOneToThreeDistinctKnownEmotions() {
        MoodInputValidator.validate(listOf("불안"), "낮음")
        MoodInputValidator.validate(listOf("불안", "무기력", "속상함"), "높음")
    }

    @Test
    fun rejectsEmptyAndMoreThanThreeEmotions() {
        assertThrows(MoodprintDataException.InvalidEmotionCount::class.java) {
            MoodInputValidator.validate(emptyList(), "보통")
        }
        assertThrows(MoodprintDataException.InvalidEmotionCount::class.java) {
            MoodInputValidator.validate(listOf("불안", "무기력", "속상함", "화남"), "보통")
        }
    }

    @Test
    fun rejectsDuplicateOrUnknownEmotionAndUnknownEnergy() {
        assertThrows(MoodprintDataException.DuplicateEmotion::class.java) {
            MoodInputValidator.validate(listOf("불안", "불안"), "보통")
        }
        assertThrows(MoodprintDataException.UnknownEmotion::class.java) {
            MoodInputValidator.validate(listOf("자동 분석된 감정"), "보통")
        }
        assertThrows(MoodprintDataException.UnknownEnergy::class.java) {
            MoodInputValidator.validate(listOf("불안"), "최고")
        }
    }

    @Test
    fun rejectsFutureRecordDateAndAcceptsTodayOrPast() {
        MoodInputValidator.validateRecordDate(recordedAtEpochMillis = 999, nowEpochMillis = 1_000)
        MoodInputValidator.validateRecordDate(recordedAtEpochMillis = 1_000, nowEpochMillis = 1_000)
        assertThrows(MoodprintDataException.FutureMoodDate::class.java) {
            MoodInputValidator.validateRecordDate(recordedAtEpochMillis = 1_001, nowEpochMillis = 1_000)
        }
    }
}
