package com.moodprint.app.util

/** 시계열 UI와 수명주기에 독립적인 행동 타이머 상태. */
data class ActionTimerState(
    val durationMillis: Long,
    val remainingMillis: Long = durationMillis,
    val runningUntilEpochMillis: Long? = null,
) {
    init {
        require(durationMillis > 0)
        require(remainingMillis in 0..durationMillis)
    }

    val isRunning: Boolean get() = runningUntilEpochMillis != null && remainingMillis > 0

    fun start(nowEpochMillis: Long): ActionTimerState = if (remainingMillis == 0L) this else copy(
        runningUntilEpochMillis = nowEpochMillis + remainingMillis,
    )

    fun snapshot(nowEpochMillis: Long): ActionTimerState {
        val deadline = runningUntilEpochMillis ?: return this
        return copy(
            remainingMillis = (deadline - nowEpochMillis).coerceIn(0, durationMillis),
            runningUntilEpochMillis = deadline.takeIf { it > nowEpochMillis },
        )
    }

    fun pause(nowEpochMillis: Long): ActionTimerState = snapshot(nowEpochMillis).copy(
        runningUntilEpochMillis = null,
    )
}
