package com.moodprint.app.util

import com.moodprint.app.MoodLog
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * 기록 탭의 "월별" 뷰에서 쓰는 통계를 계산한다.
 * UI와 무관한 순수 로직이라 테스트하기 쉽고, 추측이 아니라 실제 저장된 기록만 집계한다.
 */
object MonthlyStatsCalculator {
    data class EmotionCount(val emotion: String, val count: Int)

    data class MonthlyMoment(
        val date: LocalDate,
        val emotions: List<String>,
        val energy: String,
        val text: String,
        val actionTitle: String?,
    )

    data class MonthlyStats(
        val year: Int,
        val month: Int,
        val checkInCount: Int,
        val emotionCounts: List<EmotionCount>,
        val topEmotion: String?,
        val energyCounts: Map<String, Int>,
        val dominantEnergy: String?,
        val actionCompletionCount: Int,
        val moments: List<MonthlyMoment>,
    )

    fun calculate(
        logs: List<MoodLog>,
        anchorDate: LocalDate,
        monthOffset: Int,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): MonthlyStats {
        val targetMonth = YearMonth.from(anchorDate).plusMonths(monthOffset.toLong())
        val monthLogs = logs.filter { log ->
            YearMonth.from(Instant.ofEpochMilli(log.createdAt).atZone(zoneId).toLocalDate()) == targetMonth
        }

        val emotionCounts = monthLogs
            .flatMap { it.emotions }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { EmotionCount(it.key, it.value) }

        val energyCounts = monthLogs.groupingBy { it.energy }.eachCount()

        val moments = monthLogs
            .filter { it.note.isNotBlank() || !it.actionDetailNote.isNullOrBlank() }
            .sortedByDescending { it.createdAt }
            .map { log ->
                MonthlyMoment(
                    date = Instant.ofEpochMilli(log.createdAt).atZone(zoneId).toLocalDate(),
                    emotions = log.emotions,
                    energy = log.energy,
                    text = log.note.ifBlank { log.actionDetailNote.orEmpty() },
                    actionTitle = log.actionTitle,
                )
            }

        return MonthlyStats(
            year = targetMonth.year,
            month = targetMonth.monthValue,
            checkInCount = monthLogs.size,
            emotionCounts = emotionCounts,
            topEmotion = emotionCounts.firstOrNull()?.emotion,
            energyCounts = energyCounts,
            dominantEnergy = energyCounts.entries.maxByOrNull { it.value }?.key,
            actionCompletionCount = monthLogs.count { it.actionTitle != null },
            moments = moments,
        )
    }
}
