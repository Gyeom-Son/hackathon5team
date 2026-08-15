package com.moodprint.app.data.remote

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.moodprint.app.data.local.MoodEntryEntity
import com.moodprint.app.data.local.MoodprintDatabase
import com.moodprint.app.data.local.SyncOperationEntity
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteSyncServiceTest {
    private lateinit var database: MoodprintDatabase
    private lateinit var context: Context

    @Before fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, MoodprintDatabase::class.java).allowMainThreadQueries().build()
    }

    @After fun tearDown() { database.close() }

    @Test fun retry_drainsPersistedRoomOutbox() = runBlocking {
        database.moodEntryDao().insert(MoodEntryEntity("m1", 100, listOf("ANXIOUS"), "MEDIUM", null, "2026-08-01"))
        database.syncOperationDao().insert(SyncOperationEntity("mood:m1", 1, "/moods", "{\"clientMoodId\":\"m1\"}", 100))
        val api = FakeApi()
        val service = RemoteSyncService(AnonymousSessionStore(context), database, api)

        service.retryPending().getOrThrow()

        assertEquals(listOf("/moods"), api.paths)
        assertEquals(0, database.syncOperationDao().pending().size)
    }

    @Test fun conflict_isPreservedAsDeadLetterAndDoesNotBlockNextOperation() = runBlocking {
        database.syncOperationDao().insert(SyncOperationEntity("mood:old", 1, "/moods", "{}", 100))
        database.syncOperationDao().insert(SyncOperationEntity("mood:new", 2, "/moods", "{}", 200))
        val api = FakeApi(failFirstWith = 409)

        RemoteSyncService(AnonymousSessionStore(context), database, api).retryPending().getOrThrow()

        assertEquals(2, api.paths.size)
        assertEquals(0, database.syncOperationDao().pending().size)
        assertEquals(409, database.syncOperationDao().failed().single().httpStatus)
    }

    @Test fun unauthorized_keepsIdentityAndPendingOperations() = runBlocking {
        val store = AnonymousSessionStore(context)
        store.save("existing-token")
        database.syncOperationDao().insert(SyncOperationEntity("mood:m1", 1, "/moods", "{}", 100))
        val api = FakeApi(failFirstWith = 401)

        val result = RemoteSyncService(store, database, api).retryPending()

        assertEquals(true, result.isFailure)
        assertEquals("existing-token", store.token())
        assertEquals(listOf("mood:m1"), database.syncOperationDao().pending().map { it.id })
    }

    @Test fun invalidClientOperation_isPreservedAsDeadLetterAndDoesNotBlockNext() = runBlocking {
        database.syncOperationDao().insert(SyncOperationEntity("mood:bad", 1, "/moods", "{}", 100))
        database.syncOperationDao().insert(SyncOperationEntity("mood:next", 2, "/moods", "{}", 200))
        val api = FakeApi(failFirstWith = 400)

        RemoteSyncService(AnonymousSessionStore(context), database, api).retryPending().getOrThrow()

        assertEquals(2, api.paths.size)
        assertEquals(0, database.syncOperationDao().pending().size)
        assertEquals(400, database.syncOperationDao().failed().single().httpStatus)
    }

    @Test fun remoteDeletion_blocksWorkersUntilLocalPurgeIsFinished() = runBlocking {
        val store = AnonymousSessionStore(context)
        store.save("existing-token")
        database.syncOperationDao().insert(SyncOperationEntity("mood:m1", 1, "/moods", "{}", 100))
        val api = FakeApi()
        val service = RemoteSyncService(store, database, api)

        service.deleteRemoteIdentity().getOrThrow()

        assertEquals(1, api.deleteCount)
        assertEquals(true, store.isDeletionRequested())
        assertEquals(null, store.token())
        assertEquals(0, database.syncOperationDao().pending().size)
        service.finishDeletion()
        assertEquals(false, store.isDeletionRequested())
    }

    private class FakeApi(private val failFirstWith: Int? = null) : MoodprintRemoteApi {
        val paths = mutableListOf<String>()
        var deleteCount = 0
        override fun createAnonymousSession() = "test-token"
        override fun deleteMe(token: String) { deleteCount += 1 }
        override fun replay(token: String, operation: JSONObject) {
            paths += operation.getString("path")
            if (failFirstWith != null && paths.size == 1) throw MoodprintHttpException(failFirstWith)
        }
    }
}
