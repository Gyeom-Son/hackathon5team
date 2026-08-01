package com.moodprint.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionTimerStateTest {
    @Test fun `running timer derives remaining time from deadline`() {
        val timer = ActionTimerState(60_000).start(1_000).snapshot(21_000)
        assertEquals(40_000, timer.remainingMillis)
        assertTrue(timer.isRunning)
    }

    @Test fun `pause freezes remaining time across later snapshots`() {
        val paused = ActionTimerState(60_000).start(1_000).pause(21_000)
        assertEquals(paused, paused.snapshot(51_000))
        assertFalse(paused.isRunning)
    }

    @Test fun `expired timer clamps at zero and stops`() {
        val expired = ActionTimerState(60_000).start(1_000).snapshot(70_000)
        assertEquals(0, expired.remainingMillis)
        assertFalse(expired.isRunning)
    }

    @Test fun `paused timer resumes from frozen remainder`() {
        val resumed = ActionTimerState(60_000).start(1_000).pause(21_000).start(100_000)
        assertEquals(140_000L, resumed.runningUntilEpochMillis)
    }
}
