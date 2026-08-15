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

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteById(id: String): Int
    @Query("DELETE FROM mood_entries") suspend fun deleteAll()

    @Query("UPDATE mood_entries SET recordedLocalDate = :recordedLocalDate, emotions = :emotions, energy = :energy, note = :note WHERE id = :id")
    suspend fun updateContent(id: String, recordedLocalDate: String, emotions: List<String>, energy: String, note: String?): Int
}

@Dao
interface SyncOperationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(operation: SyncOperationEntity): Long
    @Query("UPDATE sync_operations SET path = :path, bodyJson = :bodyJson, createdAtEpochMillis = :createdAt, status = 'PENDING', httpStatus = NULL WHERE id = :id")
    suspend fun updatePayload(id: String, path: String, bodyJson: String, createdAt: Long): Int
    @Transaction
    suspend fun insertOrUpdate(operation: SyncOperationEntity) {
        if (insert(operation) == -1L) {
            check(updatePayload(operation.id, operation.path, operation.bodyJson, operation.createdAtEpochMillis) == 1)
        }
    }
    @Query("SELECT * FROM sync_operations WHERE status = 'PENDING' ORDER BY sequence") suspend fun pending(): List<SyncOperationEntity>
    @Query("SELECT * FROM sync_operations WHERE status = 'FAILED' ORDER BY sequence") suspend fun failed(): List<SyncOperationEntity>
    @Query("SELECT COUNT(*) FROM sync_operations WHERE status = 'PENDING'") fun observePendingCount(): Flow<Int>
    @Query("SELECT COUNT(*) FROM sync_operations WHERE status = 'FAILED'") fun observeFailedCount(): Flow<Int>
    @Query("UPDATE sync_operations SET status = 'FAILED', httpStatus = :httpStatus WHERE id = :id") suspend fun markFailed(id: String, httpStatus: Int): Int
    @Query("UPDATE sync_operations SET status = 'PENDING', httpStatus = NULL WHERE status = 'FAILED'") suspend fun retryFailed(): Int
    @Query("DELETE FROM sync_operations WHERE id = :id") suspend fun delete(id: String): Int
    @Query("DELETE FROM sync_operations") suspend fun clear()
    @Query("SELECT COALESCE(MAX(sequence), 0) + 1 FROM sync_operations") suspend fun nextSequence(): Long
}

@Dao
interface ActionResultDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(result: ActionResultEntity): Long

    @Query("SELECT * FROM action_results ORDER BY completedAtEpochMillis DESC")
    fun observeAll(): Flow<List<ActionResultEntity>>

    @Query("SELECT * FROM action_results WHERE moodId = :moodId ORDER BY completedAtEpochMillis")
    suspend fun getForMood(moodId: String): List<ActionResultEntity>

    @Query("SELECT * FROM action_results ORDER BY completedAtEpochMillis")
    suspend fun getAll(): List<ActionResultEntity>

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
    @Query("DELETE FROM pet_progress") suspend fun deleteAll()

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
    suspend fun insertResult(result: ActionResultEntity): Long

    @Query("SELECT * FROM action_results WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getResultBySessionId(sessionId: String): ActionResultEntity?

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

    /** 행동 결과 생성과 보상·펫 성장을 하나의 Room 트랜잭션으로 처리한다. */
    @Transaction
    suspend fun completeAction(candidate: ActionResultEntity, reward: RewardEntity): CompletionWrite {
        val result = getResultBySessionId(candidate.sessionId) ?: run {
            if (insertResult(candidate) == -1L) checkNotNull(getResultBySessionId(candidate.sessionId))
            else candidate
        }
        val applied = applyReward(reward.copy(resultId = result.id))
        return CompletionWrite(result, applied)
    }
}

data class CompletionWrite(val result: ActionResultEntity, val rewardApplied: Boolean)
