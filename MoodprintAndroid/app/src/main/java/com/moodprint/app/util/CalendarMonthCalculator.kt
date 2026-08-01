package com.moodprint.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/** UI와 무관하게 달력 그리드에 필요한 월 데이터를 계산한다. */
object CalendarMonthCalculator {
    data class MonthData(
        val year: Int,
        /** 1(월) ~ 12(월) */
        val month: Int,
        /** 일요일 0 ~ 토요일 6 */
        val firstWeekday: Int,
        val daysInMonth: Int,
        val recordedDays: Set<Int>,
    ) {
        /** 일요일부터 시작하는 6주 x 7일 UI 그리드를 반환한다. */
        fun gridCells(): List<DayCell> = List(42) { index ->
            val day = index - firstWeekday + 1
            if (day in 1..daysInMonth) {
                DayCell(dayOfMonth = day, isRecorded = day in recordedDays)
            } else {
                DayCell.empty
            }
        }
    }

    data class DayCell(
        val dayOfMonth: Int?,
        val isRecorded: Boolean,
    ) {
        fun accessibilityLabel(month: Int): String? = dayOfMonth?.let { day ->
            "${month}월 ${day}일${if (isRecorded) ", 기록 있음" else ""}"
        }

        companion object {
            val empty = DayCell(dayOfMonth = null, isRecorded = false)
        }
    }

    fun calculate(
        anchorDate: LocalDate,
        monthOffset: Int,
        recordDates: Iterable<LocalDate>,
    ): MonthData {
        val shownMonth = YearMonth.from(anchorDate).plusMonths(monthOffset.toLong())
        val firstDay = shownMonth.atDay(1)
        val recordedDays = recordDates.asSequence()
            .filter { YearMonth.from(it) == shownMonth }
            .map(LocalDate::getDayOfMonth)
            .toSet()

        return MonthData(
            year = shownMonth.year,
            month = shownMonth.monthValue,
            firstWeekday = firstDay.dayOfWeek.value % 7,
            daysInMonth = shownMonth.lengthOfMonth(),
            recordedDays = recordedDays,
        )
    }

    fun calculate(
        anchorMillis: Long,
        monthOffset: Int,
        recordTimestamps: Iterable<Long>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): MonthData = calculate(
        anchorDate = Instant.ofEpochMilli(anchorMillis).atZone(zoneId).toLocalDate(),
        monthOffset = monthOffset,
        recordDates = recordTimestamps.map {
            Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
        },
    )
}
