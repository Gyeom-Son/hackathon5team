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
import com.moodprint.app.domain.*
import com.moodprint.app.ui.model.ActionChangeUi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface DataOperation<out T> {
    data object Idle : DataOperation<Nothing>
    data object Loading : DataOperation<Nothing>
    data class Success<T>(val value: T) : DataOperation<T>
    data class Failure(val message: String) : DataOperation<Nothing>
}

data class MoodSaveResult(val moodId: String, val sessionId: String)

class MoodprintViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    private val database = MoodprintDatabase.getInstance(application)
    private val moodprintRepository: MoodprintRepository = RoomMoodprintRepository(database)
    private val profileRepository = ProfilePreferencesRepository(application)

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

    val logs: StateFlow<List<MoodLog>> = combine(moods, results) { moodEntries, actionResults ->
        moodEntries.map { mood ->
            val result = actionResults.firstOrNull { it.moodId == mood.id }
            val action = result?.actionId?.let { id -> ActionCatalog.actions.firstOrNull { it.id == id } }
            MoodLog(
                createdAt = mood.createdAtEpochMillis,
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
    }

    fun completeOnboarding(nickname: String) = viewModelScope.launch {
        profileRepository.completeOnboarding(nickname)
    }

    fun updateNickname(nickname: String) = viewModelScope.launch {
        profileRepository.updateNickname(nickname)
    }

    fun saveMood(emotions: List<String>, energy: String, note: String, recordedAtEpochMillis: Long) {
        viewModelScope.launch {
            _saveMoodState.value = DataOperation.Loading
            runCatching { moodprintRepository.saveMood(emotions, energy, note, recordedAtEpochMillis, activeSession()) }
                .onSuccess { session ->
                    savedStateHandle[ACTIVE_MOOD_ID] = session.moodId
                    savedStateHandle[ACTIVE_SESSION_ID] = session.sessionId
                    _saveMoodState.value = DataOperation.Success(
                        MoodSaveResult(session.moodId, session.sessionId)
                    )
                }
                .onFailure { error ->
                    _saveMoodState.value = DataOperation.Failure(
                        error.message ?: "마음 기록을 저장하지 못했어요."
                    )
                }
        }
    }

    fun recommendations(emotions: List<String>, energy: String): List<ActionRecommendation> {
        val selected = emotions.mapNotNull { label -> MoodEmotion.entries.firstOrNull { it.label == label } }
        val selectedEnergy = MoodEnergy.entries.firstOrNull { it.label == energy } ?: MoodEnergy.MEDIUM
        val history = results.value.map { result ->
            ActionHistory(
                actionId = result.actionId,
                change = result.change?.let { runCatching { MoodChange.valueOf(it) }.getOrNull() },
                completedAtEpochMillis = result.completedAtEpochMillis,
            )
        }
        return RecommendationService().recommend(selected, selectedEnergy, history = history)
    }

    fun finishAction(actionId: String, change: ActionChangeUi?, detailNote: String? = null) {
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
                clearActiveSession()
            }.onFailure { error ->
                _finishActionState.value = DataOperation.Failure(
                    error.message ?: "행동 완료를 저장하지 못했어요."
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
}
