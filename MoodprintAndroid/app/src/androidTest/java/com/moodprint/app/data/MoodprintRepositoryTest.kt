package com.moodprint.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.moodprint.app.data.local.MoodprintDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoodprintRepositoryTest {
    private lateinit var database: MoodprintDatabase

    @Before fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(), MoodprintDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After fun tearDown() = database.close()

    @Test fun resavingUnfinishedMood_updatesSameDraftAndRemoteIdentity() = runBlocking {
        var nextId = 0
        val repository = RoomMoodprintRepository(database, now = { 1_700_000_000_000 }, newId = { "id-${++nextId}" })
        val first = repository.saveMood(listOf("불안"), "보통", "first", 1_699_999_000_000)

        val second = repository.saveMood(listOf("외로움"), "보통", "edited", 1_699_999_000_000, first)

        assertEquals(first, second)
        assertEquals(listOf(second.moodId), database.moodEntryDao().getAll().map { it.id })
        assertEquals("edited", database.moodEntryDao().getById(second.moodId)?.note)
        assertEquals(listOf("LONELY"), database.moodEntryDao().getById(second.moodId)?.emotions)
        assertEquals(listOf("mood:${second.moodId}"), database.syncOperationDao().pending().map { it.id })
    }
}
