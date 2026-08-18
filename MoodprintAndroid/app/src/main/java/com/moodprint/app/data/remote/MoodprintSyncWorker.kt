package com.moodprint.app.data.remote

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.moodprint.app.data.local.MoodprintDatabase
import java.util.concurrent.TimeUnit

class MoodprintSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = RemoteSyncService(
        AnonymousSessionStore(applicationContext),
        MoodprintDatabase.getInstance(applicationContext),
    ).retryPending().fold(onSuccess = { Result.success() }, onFailure = { Result.retry() })

    companion object {
        private const val UNIQUE_WORK = "moodprint-room-outbox-sync"

        fun schedule(context: Context) {
            val request = OneTimeWorkRequestBuilder<MoodprintSyncWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()
            // Append behind an in-flight drain so an enqueue at the worker's completion boundary
            // cannot be lost until the next app launch.
            WorkManager.getInstance(context).enqueueUniqueWork(UNIQUE_WORK, SYNC_WORK_POLICY, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK)
        }

        internal val SYNC_WORK_POLICY = ExistingWorkPolicy.APPEND_OR_REPLACE
    }
}
