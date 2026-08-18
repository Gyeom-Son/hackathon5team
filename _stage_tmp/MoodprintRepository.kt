package com.moodprint.app.data

import com.moodprint.app.data.local.ActionResultDao
import com.moodprint.app.data.local.ActionResultEntity
import com.moodprint.app.data.local.CompletionRewardDao
import com.moodprint.app.data.local.MoodEntryDao
import com.moodprint.app.data.local.MoodEntryEntity
import com.moodprint.app.data.local.MoodprintDatabase
import com.moodprint.app.data.local.PetProgressDao
import com.moodprint.app.data.local.PetProgressEntity
import com.moodprint.app.data.local.RewardDao
import com.moodprint.app.data.local.RewardEntity
import com.moodprint.app.data.local.SyncOperationEntity
import androidx.room.withTransaction
import com.moodprint.app.domain.ActionCatalog
import com.moodprint.app.domain.MoodChange
import com.moodprint.app.domain.MoodEmotion
import com.moodprint.app.domain.MoodEnergy
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.time.Instant
import java.time.ZoneId
import org.json.JSONArray
import org.json.JSONObject

data class ActiveMoodSession(val moodId: String, val sessionId: String)

data class RewardUiData(
    val experienceAwarded: Int,
    val fragmentsAwarded: Int,
    val wasAlreadyApplied: Boolean,
    val primaryPet: PetProgressEntity,
    val collectionPet: PetProgressEntity?,
    val newlyUnlockedPet: PetProgressEntity?,
)

data class ActionCompletion(
    val result: ActionResultEntity,
    val reward: RewardUiData,
)

sealed class MoodprintDataException(message: String) : IllegalArgumentException(message) {
    class InvalidEmotionCount : MoodprintDataException("감정은 1개 이상 3개 이하로 선택해 주세요.")
    class DuplicateEmotion : MoodprintDataException("같은 감정을 중복해서 선택할 수 없어요.")
    class UnknownEmotion : MoodprintDataException("알 수 없는 감정이 포함되었어요.")
    class UnknownEnergy : MoodprintDataException("알 수 없는 에너지 단계예요.")
    class UnknownAction : MoodprintDataException("실행한 행동을 찾을 수 없어요.")
    class MissingActiveSession : MoodprintDataException("진행 중인 마음 기록을 찾을 수 없어요. 홈에서 다시 시작해 주세요.")
    class FutureMoodDate : MoodprintDataException("오늘 이후의 날짜에는 기록을 남길 수 없어요.")
}

object MoodInputValidator {
    fun validate(emotions: List<String>, energy: String) {
        if (emotions.size !in 1..3) throw MoodprintDataException.InvalidEmotionCount()
        if (emotions.distinct().size != emotions.size) throw MoodprintDataException.DuplicateEmotion()
        val knownEmotions = MoodEmotion.entries.mapTo(mutableSetOf()) { it.label }
        if (emotions.any { it !in knownEmotions }) throw MoodprintDataException.UnknownEmotion()
        if (MoodEnergy.entries.none { it.label == energy }) throw MoodprintDataException.UnknownEnergy()
    }

    fun validateRecordDate(recordedAtEpochMillis: Long, nowEpochMillis: Long) {
        if (recordedAtEpochMillis > nowEpochMillis) throw MoodprintDataException.FutureMoodDate()
    }
}

interface MoodprintRepository {
    val moods: Flow<List<MoodEntryEntity>>
    val results: Flow<List<ActionResultEntity>>
    val rewards: Flow<List<RewardEntity>>
    val pets: Flow<List<PetProgressEntity>>

    suspend fun saveMood(
        emotions: List<String>,
        energy: String,
        note: String,
        recordedAtEpochMillis: Long,
        activeSession: ActiveMoodSession? = null,
    ): ActiveMoodSession

    suspend fun finishAction(
        activeSession: ActiveMoodSession?,
        actionId: String,
        change: MoodChange?,
        detailNote: String?,
    ): ActionCompletion
}

class RoomMoodprintRepository(
    private val database: MoodprintDatabase,
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() },
) : MoodprintRepository {
    private val moodDao = database.moodEntryDao()
    private val resultDao = database.actionResultDao()
    private val rewardDao = database.rewardDao()
    private val petDao = database.petProgressDao()
    private val completionRewardDao = database.completionRewardDao()
    private val syncDao = database.syncOperationDao()

    override val moods = moodDao.observeAll()
    override val results = resultDao.observeAll()
    override val rewards = rewardDao.observeAll()
    override val pets = petDao.observeAll()

    override suspend fun saveMood(
        emotions: List<String>,
        energy: String,
        note: String,
        recordedAtEpochMillis: Long,
        activeSession: ActiveMoodSession?,
    ): ActiveMoodSession {
        MoodInputValidator.validate(emotions, energy)
        MoodInputValidator.validateRecordDate(recordedAtEpochMillis, now())
        val storedEmotions = emotions.map { label -> MoodEmotion.entries.first { it.label == label }.name }
        val storedEnergy = MoodEnergy.entries.first { it.label == energy }.name
        val recordedLocalDate = Instant.ofEpochMilli(recordedAtEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate().toString()
        return database.withTransaction {
            activeSession?.let { session ->
                val previous = moodDao.getById(session.moodId) ?: return@let
                check(resultDao.getForMood(previous.id).isEmpty())
                val updated = previous.copy(
                    emotions = storedEmotions,
                    energy = storedEnergy,
                    note = note.trim().ifEmpty { null },
                    recordedLocalDate = recordedLocalDate,
                )
                check(moodDao.updateContent(updated.id, updated.recordedLocalDate, updated.emotions, updated.energy, updated.note) == 1)
                enqueueMood(updated)
                return@withTransaction session
            }
            val session = ActiveMoodSession(moodId = newId(), sessionId = newId())
            val entry = MoodEntryEntity(
                id = session.moodId,
                createdAtEpochMillis = now(),
                emotions = storedEmotions,
                energy = storedEnergy,
                note = note.trim().ifEmpty { null },
                recordedLocalDate = recordedLocalDate,
            )
            moodDao.insert(entry)
            enqueueMood(entry)
            session
        }
    }

    override suspend fun finishAction(
        activeSession: ActiveMoodSession?,
        actionId: String,
        change: MoodChange?,
        detailNote: String?,
    ): ActionCompletion {
        val session = activeSession ?: throw MoodprintDataException.MissingActiveSession()
        if (ActionCatalog.actions.none { it.id == actionId }) {
            throw MoodprintDataException.UnknownAction()
        }
        if (moodDao.getById(session.moodId) == null) {
            throw MoodprintDataException.MissingActiveSession()
        }

        val candidate = ActionResultEntity(
                id = newId(),
                sessionId = session.sessionId,
                moodId = session.moodId,
                actionId = actionId,
                completedAtEpochMillis = now(),
                change = change?.name,
                detailNote = detailNote?.trim()?.ifEmpty { null },
            )

        val collectionBefore = petDao.getNextLocked()
        val proposedReward = RewardEntity(
            id = newId(),
            resultId = candidate.id,
            experienceAwarded = EXPERIENCE_PER_COMPLETION,
            fragmentsAwarded = FRAGMENTS_PER_COMPLETION,
            appliedAtEpochMillis = now(),
        )
        val write = database.withTransaction {
            completionRewardDao.completeAction(candidate, proposedReward).also { enqueueCompletion(it.result) }
        }
        val result = write.result
        val applied = write.rewardApplied
        val storedReward = checkNotNull(rewardDao.getByResultId(result.id))
        val primaryPet = checkNotNull(petDao.getPrimary())
        val collectionAfter = collectionBefore?.let { petDao.getById(it.id) }

        return ActionCompletion(
            result = result,
            reward = RewardUiData(
                experienceAwarded = storedReward.experienceAwarded,
                fragmentsAwarded = storedReward.fragmentsAwarded,
                wasAlreadyApplied = !applied,
                primaryPet = primaryPet,
                collectionPet = collectionAfter,
                newlyUnlockedPet = collectionAfter?.takeIf {
                    !collectionBefore.isUnlocked && it.isUnlocked
                },
            )
        )
    }

    private companion object {
        const val EXPERIENCE_PER_COMPLETION = 15
        const val FRAGMENTS_PER_COMPLETION = 1
    }

    private suspend fun enqueueMood(mood: MoodEntryEntity) {
        val body = JSONObject().apply {
            put("clientMoodId", mood.id); put("emotions", JSONArray(mood.emotions)); put("energy", mood.energy)
            put("note", mood.note ?: JSONObject.NULL); put("recordedDate", mood.recordedLocalDate)
        }
        syncDao.insertOrUpdate(SyncOperationEntity("mood:${mood.id}", syncDao.nextSequence(), "/moods", body.toString(), now()))
    }

    private suspend fun enqueueCompletion(result: ActionResultEntity) {
        val body = JSONObject().apply {
            put("sessionId", result.sessionId); put("moodId", result.moodId); put("actionId", result.actionId)
            put("change", result.change ?: JSONObject.NULL); put("detailNote", result.detailNote ?: JSONObject.NULL)
        }
        syncDao.insertOrUpdate(SyncOperationEntity("completion:${result.sessionId}", syncDao.nextSequence(), "/action-completions", body.toString(), now()))
    }
}
