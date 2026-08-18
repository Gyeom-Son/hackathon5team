package com.moodprint.app.util

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarMonthCalculatorTest {
    @Test
    fun `leap year February has 29 days and correct Sunday based offset`() {
        val result = CalendarMonthCalculator.calculate(
            anchorDate = LocalDate.of(2024, 2, 15),
            monthOffset = 0,
            recordDates = emptyList(),
        )

        assertEquals(2024, result.year)
        assertEquals(2, result.month)
        assertEquals(4, result.firstWeekday)
        assertEquals(29, result.daysInMonth)
    }

    @Test
    fun `month offset crosses year boundary`() {
        val result = CalendarMonthCalculator.calculate(
            anchorDate = LocalDate.of(2026, 1, 31),
            monthOffset = -1,
            recordDates = emptyList(),
        )

        assertEquals(2025, result.year)
        assertEquals(12, result.month)
        assertEquals(31, result.daysInMonth)
    }

    @Test
    fun `only dates in displayed month are recorded and duplicates collapse`() {
        val result = CalendarMonthCalculator.calculate(
            anchorDate = LocalDate.of(2026, 7, 31),
            monthOffset = 0,
            recordDates = listOf(
                LocalDate.of(2026, 6, 30),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 8, 1),
            ),
        )

        assertEquals(setOf(1, 31), result.recordedDays)
    }

    @Test
    fun `timestamps are assigned using requested timezone`() {
        val utc = ZoneId.of("UTC")
        val seoul = ZoneId.of("Asia/Seoul")
        val timestamp = LocalDate.of(2026, 8, 1)
            .atStartOfDay(utc)
            .minusHours(1)
            .toInstant()
            .toEpochMilli()

        val result = CalendarMonthCalculator.calculate(
            anchorMillis = LocalDate.of(2026, 8, 15).atStartOfDay(seoul).toInstant().toEpochMilli(),
            monthOffset = 0,
            recordTimestamps = listOf(timestamp),
            zoneId = seoul,
        )

        assertEquals(setOf(1), result.recordedDays)
    }

    @Test
    fun `grid always has 42 cells and aligns first day`() {
        val month = CalendarMonthCalculator.calculate(
            LocalDate.of(2026, 8, 1), 0, listOf(LocalDate.of(2026, 8, 3)),
        )

        val cells = month.gridCells()
        assertEquals(42, cells.size)
        assertEquals(1, cells[6].dayOfMonth)
        assertEquals(3, cells[8].dayOfMonth)
        assertEquals(true, cells[8].isRecorded)
        assertEquals(null, cells.first().dayOfMonth)
    }
}
