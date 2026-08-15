package com.moodprint.app.data.remote

import androidx.work.ExistingWorkPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class MoodprintSyncWorkerPolicyTest {
    @Test fun newOutboxTrigger_isAppendedBehindRunningWorker() {
        assertEquals(ExistingWorkPolicy.APPEND_OR_REPLACE, MoodprintSyncWorker.SYNC_WORK_POLICY)
    }
}
