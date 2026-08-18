package com.moodprint.app.ui.records

import com.moodprint.app.MoodLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MoodLogStableKeyTest {
    @Test fun persistedId_isPreferredAsStableKey() {
        assertEquals("mood-1", MoodLog(id = "mood-1", emotions = listOf("불안"), energy = "낮음", note = "").stableKey)
    }

    @Test fun fallbackSeparatesDifferentLogsAtSameTimestamp() {
        val first = MoodLog(createdAt = 10L, recordedLocalDate = "2026-08-01", emotions = listOf("불안"), energy = "낮음", note = "")
        val second = MoodLog(createdAt = 10L, recordedLocalDate = "2026-08-01", emotions = listOf("슬픔"), energy = "낮음", note = "")

        assertNotEquals(first.stableKey, second.stableKey)
    }
}
