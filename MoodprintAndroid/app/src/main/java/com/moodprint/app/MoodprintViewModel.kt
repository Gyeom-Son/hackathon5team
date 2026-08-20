package com.moodprint.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.moodprint.app.data.ActionCompletion
import com.moodprint.app.data.ActiveMoodSession
import com.moodprint.app.data.MoodprintRepository
import com.moodprint.app.data.RoomMoodprintRepository
import com.moodprint.app.data.local.*
import com.moodprint.app.data.preferences.ProfilePreferences
import com.moodprint.app.data.preferences.ProfilePreferencesRepository
import com.moodprint.app.data.remote.MoodprintSyncWorker
import com.moodprint.app.data.remote.AnonymousSessionStore
import com.moodprint.app.data.remote.RemoteSyncService
import com.moodprint.app.data.remote.SyncStatus
import com.moodprint.app.domain.*
import com.moodprint.app.ui.model.ActionChangeUi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface DataOperation<out T> {
    data object Idle : DataOperation<Nothing>
    data object Loading : DataOperation<Nothing>
    data class Success<T>(val value: T, val eventId: String = UUID.randomUUID().toString()) : DataOperation<T>
    data class Failure(val operation: DataOperationKind, val message: String) : DataOperation<Nothing>
}

enum class DataOperationKind { SAVE_MOOD, FINISH_ACTION }

data class MoodSaveResult(val moodId: String, val sessionId: String)

class MoodprintViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val database = MoodprintDatabase.getInstance(application)
    private val moodprintRepository: MoodprintRepository = RoomMoodprintRepository(database)
    private val profileRepository = ProfilePreferencesRepository(application)
    private val remoteSyncService = RemoteSyncService(AnonymousSessionStore(application), database)
    private data class SaveMoodRequest(val emotions: List<String>, val energy: String, val note: String, val recordedAtEpochMillis: Long)
    private data class FinishActionRequest(val actionId: String, val change: ActionChangeUi?, val detailNote: String?)
    private var lastSaveRequest: SaveMoodRequest? = null
    private var lastFinishRequest: FinishActionRequest? = null

    private val _saveMoodState = MutableStateFlow<DataOperation<MoodSaveResult>>(DataOperation.Idle)
    val saveMoodState: StateFlow<DataOperation<MoodSaveResult>> = _saveMoodState.asStateFlow()
    private val _finishActionState = MutableStateFlow<DataOperation<ActionCompletion>>(DataOperation.Idle)
    val finishActionState: StateFlow<DataOperation<ActionCompletion>> = _finishActionState.asStateFlow()

    val profile = profileRepository.preferences.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfilePreferences(false, "", loaded = false)
    )
    val moods = moodprintRepository.moods.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val results = moodprintRepository.results.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val pets = moodprintRepository.pets.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val rewards = moodprintRepository.rewards.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    val syncEnabled: Boolean = BuildConfig.MOODPRINT_REMOTE_SYNC_ENABLED
    private val _syncInProgress = MutableStateFlow(false)
    val syncInProgress: StateFlow<Boolean> = _syncInProgress.asStateFlow()
    private val _deleteInProgress = MutableStateFlow(false)
    val deleteInProgress: StateFlow<Boolean> = _deleteInProgress.asStateFlow()
    private val _dataManagementMessage = MutableStateFlow<String?>(null)
    val dataManagementMessage: StateFlow<String?> = _dataManagementMessage.asStateFlow()
    val syncStatusText: StateFlow<String> = remoteSyncService.status.map { status ->
        when (status) {
            SyncStatus.Disabled -> "서버 동기화 꺼짐 · 이 기기에 저장됨"
            SyncStatus.Synced -> "서버에 기록 사본 저장됨"
            SyncStatus.Deleting -> "전체 기록 삭제 처리 중"
            is SyncStatus.Pending -> "서버 동기화 대기 ${status.count}건"
            is SyncStatus.Failed -> "동기화 확인 필요 ${status.failedCount}건"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), if (syncEnabled) "동기화 상태 확인 중" else "서버 동기화 꺼짐 · 이 기기에 저장됨")
    val syncRequiresReconnect: StateFlow<Boolean> = remoteSyncService.status.map { status ->
        status is SyncStatus.Failed && status.requiresReconnect
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val logs: StateFlow<List<MoodLog>> = combine(moods, results) { moodEntries, actionResults ->
        val resultsByMood = actionResults.associateBy { it.moodId }
        moodEntries.map { mood ->
            val result = resultsByMood[mood.id]
            val action = result?.actionId?.let { id -> ActionCatalog.actions.firstOrNull { it.id == id } }
            MoodLog(
                id = mood.id,
                createdAt = mood.createdAtEpochMillis,
                recordedLocalDate = mood.recordedLocalDate,
                emotions = mood.emotions.map { raw -> runCatching { MoodEmotion.valueOf(raw).label }.getOrDefault(raw) },
                energy = runCatching { MoodEnergy.valueOf(mood.energy).label }.getOrDefault(mood.energy),
                note = mood.note.orEmpty(),
                actionTitle = action?.title,
                actionDetailPrompt = action?.detailPrompt,
                actionDetailNote = result?.detailNote,
                changeLabel = result?.change?.let { raw -> runCatching { MoodChange.valueOf(raw).label }.getOrNull() }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * 감정+행동 조합 중 실제로 여러 번 긍정적인 변화로 이어졌던 조합을 찾아 개인화 문구를 만든다.
     * 근거가 될 기록이 충분하지 않으면 null이라 홈 화면에서 아무 것도 표시하지 않는다.
     */
    val personalizationInsight: StateFlow<PersonalizationInsight?> = combine(moods, results) { moodEntries, actionResults ->
        val moodsById = moodEntries.associateBy { it.id }
        val outcomes = actionResults.mapNotNull { result ->
            val mood = moodsById[result.moodId] ?: return@mapNotNull null
            val emotions = mood.emotions.mapNotNull { raw -> runCatching { MoodEmotion.valueOf(raw) }.getOrNull() }
            val change = result.change?.let { raw -> runCatching { MoodChange.valueOf(raw) }.getOrNull() }
            MoodOutcome(emotions = emotions, actionId = result.actionId, change = change)
        }
        PersonalizationInsightService.bestInsight(outcomes)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        val hasMood = savedStateHandle.get<String>(ACTIVE_MOOD_ID) != null
        val hasSession = savedStateHandle.get<String>(ACTIVE_SESSION_ID) != null
        if (hasMood != hasSession) clearActiveSession()
        MoodprintSyncWorker.schedule(application)
    }

    fun completeOnboarding(nickname: String) = viewModelScope.launch {
        profileRepository.completeOnboarding(nickname)
    }

    fun updateNickname(nickname: String) = viewModelScope.launch {
        profileRepository.updateNickname(nickname)
    }

    /** 해금된 동물을 홈 화면 대표 동반자로 바꾼다. 각 펫의 레벨·경험치는 서로 독립적으로 유지된다. */
    fun setPrimaryPet(petId: String) = viewModelScope.launch {
        moodprintRepository.setPrimaryPet(petId)
    }

    fun retrySync() {
        if (_syncInProgress.value || _deleteInProgress.value || !syncEnabled) return
        viewModelScope.launch {
            _syncInProgress.value = true
            remoteSyncService.retryFailedAndPending()
                .onFailure { _dataManagementMessage.value = syncFailureMessage(it) }
            _syncInProgress.value = false
        }
    }

    fun reconnectSync() {
        if (_syncInProgress.value || _deleteInProgress.value || !syncEnabled) return
        viewModelScope.launch {
            _syncInProgress.value = true
            remoteSyncService.reconnectAsNewAnonymousIdentity()
                .onSuccess { _dataManagementMessage.value = "새 익명 서버 저장 연결을 시작했어요. 이 기기의 기록은 그대로 유지돼요." }
                .onFailure { _dataManagementMessage.value = syncFailureMessage(it) }
            _syncInProgress.value = false
        }
    }

    fun deleteAllDataIncludingServer() {
        if (_deleteInProgress.value) return
        viewModelScope.launch {
            _deleteInProgress.value = true
            _dataManagementMessage.value = null
            val remote = if (syncEnabled) remoteSyncService.deleteRemoteIdentity() else Result.success(Unit)
            remote.onSuccess {
                MoodprintSyncWorker.cancel(getApplication())
                database.resetUserData()
                profileRepository.clear()
                clearActiveSession()
                remoteSyncService.finishDeletion()
                _saveMoodState.value = DataOperation.Idle
                _finishActionState.value = DataOperation.Idle
                _dataManagementMessage.value = "이 기기와 서버에 연결된 기록을 모두 삭제했어요."
            }.onFailure {
                _dataManagementMessage.value = deleteFailureMessage(it)
            }
            _deleteInProgress.value = false
        }
    }

    fun clearDataManagementMessage() { _dataManagementMessage.value = null }

    private fun syncFailureMessage(error: Throwable): String = when {
        error is com.moodprint.app.data.remote.MoodprintHttpException && error.status == 429 ->
            "요청이 잠시 모였어요. ${error.retryAfterSeconds ?: 60}초 후에 다시 시도해 주세요."
        error is com.moodprint.app.data.remote.MoodprintHttpException && error.status == 401 ->
            "서버 저장 연결을 확인해야 해요. 기록은 이 기기에 안전하게 유지돼요."
        else -> "서버 동기화를 완료하지 못했어요. 네트워크를 확인하고 다시 시도해 주세요."
    }

    private fun deleteFailureMessage(error: Throwable): String = when {
        error is com.moodprint.app.data.remote.MoodprintHttpException && error.status == 429 ->
            "서버가 잠시 바빠 삭제를 확인하지 못했어요. 기록은 유지되며 ${error.retryAfterSeconds ?: 60}초 후 다시 시도할 수 있어요."
        else -> "서버 기록 삭제를 확인하지 못했어요. 기록은 유지되며 다시 시도할 수 있어요."
    }

    fun saveMood(emotions: List<String>, energy: String, note: String, recordedAtEpochMillis: Long) {
        if (_saveMoodState.value is DataOperation.Loading) return
        lastSaveRequest = SaveMoodRequest(emotions.toList(), energy, note, recordedAtEpochMillis)
        viewModelScope.launch {
            _saveMoodState.value = DataOperation.Loading
            runCatching { moodprintRepository.saveMood(emotions, energy, note, recordedAtEpochMillis, activeSession()) }
                .onSuccess { session ->
                    savedStateHandle[ACTIVE_MOOD_ID] = session.moodId
                    savedStateHandle[ACTIVE_SESSION_ID] = session.sessionId
                    _saveMoodState.value = DataOperation.Success(
                        MoodSaveResult(session.moodId, session.sessionId)
                    )
                    // The Room transaction already persisted the outbox row. Schedule immediately;
                    // UI success never waits for a network timeout.
                    MoodprintSyncWorker.schedule(getApplication())
                }
                .onFailure { error ->
                    _saveMoodState.value = DataOperation.Failure(
                        DataOperationKind.SAVE_MOOD,
                        userSafeMessage(error, "마음 기록을 저장하지 못했어요. 다시 시도해 주세요.")
                    )
                }
        }
    }

    fun recommendations(emotions: List<String>, energy: String): List<ActionRecommendation> {
        val selected = emotions.mapNotNull { label -> MoodEmotion.entries.firstOrNull { it.label == label } }
        val selectedEnergy = MoodEnergy.entries.firstOrNull { it.label == energy } ?: MoodEnergy.MEDIUM
        val moodsById = moods.value.associateBy { it.id }
        val history = results.value.map { result ->
            val mood = moodsById[result.moodId]
            ActionHistory(
                actionId = result.actionId,
                change = result.change?.let { runCatching { MoodChange.valueOf(it) }.getOrNull() },
                completedAtEpochMillis = result.completedAtEpochMillis,
                emotions = mood?.emotions?.mapNotNull { raw -> runCatching { MoodEmotion.valueOf(raw) }.getOrNull() }?.toSet().orEmpty(),
                energy = mood?.energy?.let { raw -> runCatching { MoodEnergy.valueOf(raw) }.getOrNull() },
            )
        }
        return RecommendationService().recommend(selected, selectedEnergy, history = history)
    }

    fun finishAction(actionId: String, change: ActionChangeUi?, detailNote: String? = null) {
        if (_finishActionState.value is DataOperation.Loading) return
        lastFinishRequest = FinishActionRequest(actionId, change, detailNote)
        viewModelScope.launch {
            _finishActionState.value = DataOperation.Loading
            val domainChange = change?.takeUnless { it == ActionChangeUi.SKIP }?.let {
                MoodChange.valueOf(it.name)
            }
            runCatching {
                moodprintRepository.finishAction(
                    activeSession = activeSession(),
                    actionId = actionId,
                    change = domainChange,
                    detailNote = detailNote,
                )
            }.onSuccess { completion ->
                _finishActionState.value = DataOperation.Success(completion)
                MoodprintSyncWorker.schedule(getApplication())
                clearActiveSession()
            }.onFailure { error ->
                _finishActionState.value = DataOperation.Failure(
                    DataOperationKind.FINISH_ACTION,
                    userSafeMessage(error, "행동 완료를 저장하지 못했어요. 다시 시도해 주세요.")
                )
            }
        }
    }

    fun resetSaveMoodState() {
        _saveMoodState.value = DataOperation.Idle
    }

    fun resetFinishActionState() {
        _finishActionState.value = DataOperation.Idle
    }

    fun retry(operation: DataOperationKind) {
        when (operation) {
            DataOperationKind.SAVE_MOOD -> lastSaveRequest?.let { saveMood(it.emotions, it.energy, it.note, it.recordedAtEpochMillis) }
            DataOperationKind.FINISH_ACTION -> lastFinishRequest?.let { finishAction(it.actionId, it.change, it.detailNote) }
        }
    }

    fun dismissFailure(operation: DataOperationKind) {
        when (operation) {
            DataOperationKind.SAVE_MOOD -> if (_saveMoodState.value is DataOperation.Failure) resetSaveMoodState()
            DataOperationKind.FINISH_ACTION -> if (_finishActionState.value is DataOperation.Failure) resetFinishActionState()
        }
    }

    fun beginNewMood() {
        clearActiveSession()
        resetSaveMoodState()
        resetFinishActionState()
    }

    private fun activeSession(): ActiveMoodSession? {
        val moodId = savedStateHandle.get<String>(ACTIVE_MOOD_ID) ?: return null
        val sessionId = savedStateHandle.get<String>(ACTIVE_SESSION_ID) ?: return null
        return ActiveMoodSession(moodId, sessionId)
    }

    private fun clearActiveSession() {
        savedStateHandle.remove<String>(ACTIVE_MOOD_ID)
        savedStateHandle.remove<String>(ACTIVE_SESSION_ID)
    }

    private companion object {
        const val ACTIVE_MOOD_ID = "active_mood_id"
        const val ACTIVE_SESSION_ID = "active_session_id"
    }

    private fun userSafeMessage(error: Throwable, fallback: String): String =
        if (error is com.moodprint.app.data.MoodprintDataException) error.message ?: fallback else fallback
}
