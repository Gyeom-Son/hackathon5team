package com.moodprint.app.data.remote

import com.moodprint.app.BuildConfig
import com.moodprint.app.data.local.MoodprintDatabase
import com.moodprint.app.data.local.SyncOperationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

sealed interface SyncStatus {
    data object Disabled : SyncStatus
    data object Synced : SyncStatus
    data object Deleting : SyncStatus
    data class Pending(val count: Int) : SyncStatus
    data class Failed(val pendingCount: Int, val failedCount: Int, val requiresReconnect: Boolean) : SyncStatus
}

/** Room-backed ordered outbox. */
class RemoteSyncService(
    private val sessionStore: AnonymousSessionStore,
    private val database: MoodprintDatabase,
    private val api: MoodprintRemoteApi = MoodprintApiClient(),
) {
    private val outbox get() = database.syncOperationDao()
    val status: Flow<SyncStatus> = combine(
        outbox.observePendingCount(), outbox.observeFailedCount(), outbox.observeUnauthorizedCount(), sessionStore.deletionRequested,
    ) { pending, failed, unauthorized, deleting ->
        when {
            !BuildConfig.MOODPRINT_REMOTE_SYNC_ENABLED -> SyncStatus.Disabled
            deleting -> SyncStatus.Deleting
            failed > 0 -> SyncStatus.Failed(pending, failed, unauthorized > 0)
            pending > 0 -> SyncStatus.Pending(pending)
            else -> SyncStatus.Synced
        }
    }

    suspend fun uploadMood(mood: RemoteMood): Result<Unit> = enqueue("mood:${mood.clientMoodId}", "/moods", moodBody(mood))

    suspend fun uploadCompletion(completion: RemoteCompletion): Result<Unit> = enqueue("completion:${completion.sessionId}",
        "/action-completions", JSONObject().apply {
            put("sessionId", completion.sessionId); put("moodId", completion.moodId)
            put("actionId", completion.actionId); put("change", completion.change?.name ?: JSONObject.NULL)
            put("detailNote", completion.detailNote ?: JSONObject.NULL)
        },
    )

    suspend fun retryPending(): Result<Unit> = withContext(Dispatchers.IO) { processMutex.withLock {
        if (sessionStore.isDeletionRequested()) return@withLock Result.success(Unit)
        importLegacyOutbox()
        drain()
    } }

    suspend fun retryFailedAndPending(): Result<Unit> = withContext(Dispatchers.IO) { processMutex.withLock {
        if (sessionStore.isDeletionRequested()) return@withLock Result.failure(IllegalStateException("삭제 처리 중이에요."))
        outbox.retryFailed()
        drain()
    } }

    /** 사용자가 선택했을 때만 만료된 익명 서버 연결을 새 연결로 바꾼다. 로컬 기록은 그대로 유지한다. */
    suspend fun reconnectAsNewAnonymousIdentity(): Result<Unit> = withContext(Dispatchers.IO) { processMutex.withLock {
        if (sessionStore.isDeletionRequested()) return@withLock Result.failure(IllegalStateException("삭제 처리 중이에요."))
        sessionStore.clearToken()
        outbox.retryFailed()
        drain()
    } }

    suspend fun deleteRemoteIdentity(): Result<Unit> = withContext(Dispatchers.IO) {
        sessionStore.setDeletionRequested(true)
        processMutex.withLock {
            runCatching {
                if (!BuildConfig.MOODPRINT_REMOTE_SYNC_ENABLED) error("서버 동기화가 비활성화되어 있어요.")
                sessionStore.token()?.let { token ->
                    try { api.deleteMe(token) }
                    catch (error: MoodprintHttpException) {
                        // DELETE is idempotent from the client perspective: a missing/expired
                        // identity means there is no reachable server data left for this token.
                        if (error.status != 401 && error.status != 404) throw error
                    }
                }
                outbox.clear()
                sessionStore.clearToken()
            }
        }
    }

    suspend fun finishDeletion() = sessionStore.clearAll()
    suspend fun cancelDeletion() = sessionStore.setDeletionRequested(false)

    private suspend fun enqueue(id: String, path: String, body: JSONObject): Result<Unit> = withContext(Dispatchers.IO) {
        processMutex.withLock {
            if (sessionStore.isDeletionRequested()) return@withLock Result.failure(IllegalStateException("삭제 처리 중에는 동기화할 수 없어요."))
            outbox.insert(SyncOperationEntity(id, outbox.nextSequence(), path, body.toString(), System.currentTimeMillis()))
            drain()
        }
    }

    private suspend fun importLegacyOutbox() {
        sessionStore.pending().forEach { legacy ->
            val path = legacy.optString("path")
            val body = legacy.optJSONObject("body") ?: return@forEach
            if (path.isNotBlank()) outbox.insert(SyncOperationEntity(UUID.randomUUID().toString(), outbox.nextSequence(), path, body.toString(), System.currentTimeMillis()))
        }
        sessionStore.clearPending()
    }

    private suspend fun drain(): Result<Unit> = runCatching {
        if (!BuildConfig.MOODPRINT_REMOTE_SYNC_ENABLED) return@runCatching
        val token = token()
        while (true) {
            val operation = outbox.pending().firstOrNull() ?: break
            try {
                api.replay(token, JSONObject().put("path", operation.path).put("body", JSONObject(operation.bodyJson)))
                outbox.delete(operation.id)
            } catch (error: MoodprintHttpException) {
                when {
                    // Never create a replacement anonymous identity automatically. Doing so
                    // would leave the old identity and its private records unreachable.
                    error.status == 401 -> {
                        check(outbox.markFailed(operation.id, error.status) == 1)
                        throw error
                    }
                    error.status == 409 ->
                        check(outbox.markFailed(operation.id, error.status) == 1)
                    error.status in 400..499 && error.status != 429 ->
                        check(outbox.markFailed(operation.id, error.status) == 1)
                    else -> throw error
                }
            }
        }
    }

    private suspend fun token(): String = sessionStore.token() ?: api.createAnonymousSession().also { sessionStore.save(it) }

    private fun moodBody(mood: RemoteMood) = JSONObject().apply {
        put("clientMoodId", mood.clientMoodId); put("emotions", JSONArray(mood.emotions)); put("energy", mood.energy)
        put("note", mood.note ?: JSONObject.NULL)
        put("recordedDate", java.time.Instant.ofEpochMilli(mood.recordedAtEpochMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString())
    }

    private companion object { val processMutex = Mutex() }
}
