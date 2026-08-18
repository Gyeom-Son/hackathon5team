package com.moodprint.app.util

import com.moodprint.app.MoodLog
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MonthlyStatsCalculatorTest {
    private val zone = ZoneId.systemDefault()

    private fun millisOn(date: LocalDate) = date.atStartOfDay(zone).toInstant().toEpochMilli()

    @Test fun onlyCountsLogsInTargetMonth() {
        val logs = listOf(
            MoodLog(createdAt = millisOn(LocalDate.of(2026, 8, 1)), emotions = listOf("불안"), energy = "낮음", note = ""),
            MoodLog(createdAt = millisOn(LocalDate.of(2026, 8, 15)), emotions = listOf("무기력"), energy = "보통", note = ""),
            MoodLog(createdAt = millisOn(LocalDate.of(2026, 7, 31)), emotions = listOf("슬픔"), energy = "낮음", note = ""),
        )

        val stats = MonthlyStatsCalculator.calculate(logs, LocalDate.of(2026, 8, 20), monthOffset = 0, zoneId = zone)

        assertEquals(2026, stats.year)
        assertEquals(8, stats.month)
        assertEquals(2, stats.checkInCount)
    }

    @Test fun ranksTopEmotionByFrequency() {
        val logs = listOf(
            MoodLog(createdAt = millisOn(LocalDate.of(2026, 8, 1)), emotions = listOf("불안", "복잡함"), energy = "낮음", note = ""),
            MoodLog(createdAt = millisOn(LocalDate.of(2026, 8, 2)), emotions = listOf("불안"), energy = "보통", note = ""),
            MoodLog(createdAt = millisOn(LocalDate.of(2026, 8, 3)), emotions = listOf("슬픔"), energy = "낮음", note = ""),
        )

        val stats = MonthlyStatsCalculator.calculate(logs, LocalDate.of(2026, 8, 10), monthOffset = 0, zoneId = zone)

        assertEquals("불안", stats.topEmotion)
        assertEquals(2, stats.emotionCounts.first { it.emotion == "불안" }.count)
    }

    @Test fun momentsOnlyIncludeLogsWithNarrativeContent() {
        val logs = listOf(
            MoodLog(createdAt = millisOn(LocalDate.of(2026, 8, 1)), emotions = listOf("불안"), energy = "낮음", note = "오늘은 힘들었다"),
            MoodLog(createdAt = millisOn(LocalDate.of(2026, 8, 2)), emotions = listOf("무기력"), energy = "보통", note = ""),
        )

        val stats = MonthlyStatsCalculator.calculate(logs, LocalDate.of(2026, 8, 10), monthOffset = 0, zoneId = zone)

        assertEquals(1, stats.moments.size)
        assertEquals("오늘은 힘들었다", stats.moments.first().text)
    }

    @Test fun emptyMonthReturnsNullTopEmotionAndDominantEnergy() {
        val stats = MonthlyStatsCalculator.calculate(emptyList(), LocalDate.of(2026, 8, 10), monthOffset = 0, zoneId = zone)

        assertEquals(0, stats.checkInCount)
        assertNull(stats.topEmotion)
        assertNull(stats.dominantEnergy)
    }

    @Test fun monthOffsetMovesToPreviousMonth() {
        val logs = listOf(
            MoodLog(createdAt = millisOn(LocalDate.of(2026, 7, 15)), emotions = listOf("지침"), energy = "낮음", note = ""),
        )

        val stats = MonthlyStatsCalculator.calculate(logs, LocalDate.of(2026, 8, 10), monthOffset = -1, zoneId = zone)

        assertEquals(7, stats.month)
        assertEquals(1, stats.checkInCount)
    }
}
