package com.moodprint.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEntryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: MoodEntryEntity)

    @Query("SELECT * FROM mood_entries ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<MoodEntryEntity>>

    @Query("SELECT * FROM mood_entries ORDER BY createdAtEpochMillis DESC")
    suspend fun getAll(): List<MoodEntryEntity>

    @Query("SELECT * FROM mood_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MoodEntryEntity?

    @Query("UPDATE mood_entries SET createdAtEpochMillis = :createdAtEpochMillis, emotions = :emotions, energy = :energy, note = :note WHERE id = :id")
    suspend fun updateContent(id: String, createdAtEpochMillis: Long, emotions: List<String>, energy: String, note: String?): Int
}

@Dao
interface ActionResultDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(result: ActionResultEntity): Long

    @Query("SELECT * FROM action_results ORDER BY completedAtEpochMillis DESC")
    fun observeAll(): Flow<List<ActionResultEntity>>

    @Query("SELECT * FROM action_results WHERE moodId = :moodId ORDER BY completedAtEpochMillis")
    suspend fun getForMood(moodId: String): List<ActionResultEntity>

    @Query("SELECT * FROM action_results WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getBySessionId(sessionId: String): ActionResultEntity?
}

@Dao
interface RewardDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reward: RewardEntity): Long

    @Query("SELECT * FROM rewards WHERE resultId = :resultId LIMIT 1")
    suspend fun getByResultId(resultId: String): RewardEntity?

    @Query("SELECT * FROM rewards ORDER BY appliedAtEpochMillis DESC")
    fun observeAll(): Flow<List<RewardEntity>>
}

@Dao
interface PetProgressDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(pets: List<PetProgressEntity>)

    @Query("SELECT * FROM pet_progress ORDER BY isPrimary DESC, isUnlocked DESC, name")
    fun observeAll(): Flow<List<PetProgressEntity>>

    @Query("SELECT * FROM pet_progress ORDER BY isPrimary DESC, isUnlocked DESC, name")
    suspend fun getAll(): List<PetProgressEntity>

    @Query("SELECT * FROM pet_progress WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PetProgressEntity?

    @Query("SELECT * FROM pet_progress WHERE isPrimary = 1 LIMIT 1")
    suspend fun getPrimary(): PetProgressEntity?

    @Query("SELECT * FROM pet_progress WHERE isPrimary = 0 AND isUnlocked = 0 ORDER BY CASE id WHEN '4B0EE180-65EB-4703-89EA-F695DF421102' THEN 1 WHEN '4B0EE180-65EB-4703-89EA-F695DF421103' THEN 2 ELSE 3 END LIMIT 1")
    suspend fun getNextLocked(): PetProgressEntity?

    @Query(
        """
        UPDATE pet_progress
        SET experience = experience + :amount,
            level = ((experience + :amount) / 100) + 1
        WHERE id = :petId
        """
    )
    suspend fun addExperience(petId: String, amount: Int): Int

    @Query(
        """
        UPDATE pet_progress
        SET fragments = MIN(requiredFragments, fragments + :amount),
            isUnlocked = CASE WHEN fragments + :amount >= requiredFragments THEN 1 ELSE isUnlocked END
        WHERE id = :petId
        """
    )
    suspend fun addFragments(petId: String, amount: Int): Int
}

/**
 * Reward row creation and pet progression share one Room transaction. Inserting the same
 * resultId returns false and never applies experience or fragments a second time.
 */
@Dao
interface CompletionRewardDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReward(reward: RewardEntity): Long

    @Query("SELECT id FROM pet_progress WHERE isPrimary = 1 LIMIT 1")
    suspend fun getPrimaryPetId(): String?

    @Query("SELECT id FROM pet_progress WHERE isPrimary = 0 AND isUnlocked = 0 ORDER BY CASE id WHEN '4B0EE180-65EB-4703-89EA-F695DF421102' THEN 1 WHEN '4B0EE180-65EB-4703-89EA-F695DF421103' THEN 2 ELSE 3 END LIMIT 1")
    suspend fun getNextLockedPetId(): String?

    @Query(
        """
        UPDATE pet_progress
        SET experience = experience + :amount,
            level = ((experience + :amount) / 100) + 1
        WHERE id = :petId
        """
    )
    suspend fun addExperience(petId: String, amount: Int): Int

    @Query(
        """
        UPDATE pet_progress
        SET fragments = MIN(requiredFragments, fragments + :amount),
            isUnlocked = CASE WHEN fragments + :amount >= requiredFragments THEN 1 ELSE isUnlocked END
        WHERE id = :petId
        """
    )
    suspend fun addFragments(petId: String, amount: Int): Int

    @Transaction
    suspend fun applyReward(reward: RewardEntity): Boolean {
        require(reward.experienceAwarded >= 0 && reward.fragmentsAwarded >= 0)
        val primaryPetId = checkNotNull(getPrimaryPetId()) { "Primary pet is not seeded" }
        val collectionPetId = getNextLockedPetId()
        val appliedReward = reward.copy(
            fragmentsAwarded = if (collectionPetId == null) 0 else reward.fragmentsAwarded
        )
        if (insertReward(appliedReward) == -1L) return false
        check(addExperience(primaryPetId, appliedReward.experienceAwarded) == 1)
        collectionPetId?.let { petId ->
            check(addFragments(petId, reward.fragmentsAwarded) == 1)
        }
        return true
    }
}
