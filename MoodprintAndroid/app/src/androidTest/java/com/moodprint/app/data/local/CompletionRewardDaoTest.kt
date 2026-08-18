package com.moodprint.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompletionRewardDaoTest {
    private lateinit var database: MoodprintDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MoodprintDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun sameResultNeverRewardsTwice() = runBlocking {
        seedPets()
        val result = seedResult()
        val first = RewardEntity("reward-1", result.id, 15, 1, 100)
        val duplicate = RewardEntity("reward-2", result.id, 15, 1, 200)

        assertTrue(database.completionRewardDao().applyReward(first))
        assertFalse(database.completionRewardDao().applyReward(duplicate))

        assertEquals(15, database.petProgressDao().getById(StablePetIds.MONGSIL)?.experience)
        assertEquals(1, database.petProgressDao().getById(StablePetIds.POLJJAK)?.fragments)
    }

    @Test
    fun missingPrimaryPetRollsBackRewardInsert() = runBlocking {
        val result = seedResult()
        val reward = RewardEntity("reward-failed", result.id, 15, 1, 100)

        runCatching { database.completionRewardDao().applyReward(reward) }

        assertNull(database.rewardDao().getByResultId(result.id))
    }

    @Test
    fun completeActionRollsBackResultWhenRewardCannotBeApplied() = runBlocking {
        val mood = MoodEntryEntity("atomic-mood", 1, listOf("ANXIOUS"), "MEDIUM", null)
        database.moodEntryDao().insert(mood)
        val result = ActionResultEntity("atomic-result", "atomic-session", mood.id, "action", 2, null, null)
        val reward = RewardEntity("atomic-reward", result.id, 15, 1, 3)

        runCatching { database.completionRewardDao().completeAction(result, reward) }

        assertNull(database.actionResultDao().getBySessionId(result.sessionId))
        assertNull(database.rewardDao().getByResultId(result.id))
    }

    private suspend fun seedResult(): ActionResultEntity {
        val mood = MoodEntryEntity("mood", 1, listOf("불안"), "보통", null)
        database.moodEntryDao().insert(mood)
        return ActionResultEntity("result", "session", mood.id, "action", 2, null, null)
            .also { database.actionResultDao().insert(it) }
    }

    private suspend fun seedPets() {
        database.petProgressDao().insertAll(
            listOf(
                PetProgressEntity(StablePetIds.MONGSIL, "몽실이", "lavender", 1, 0, 3, 3, true, true),
                PetProgressEntity(StablePetIds.POLJJAK, "폴짝이", "mint", 0, 0, 0, 3, false, false),
            )
        )
    }
}
