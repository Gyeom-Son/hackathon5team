package com.moodprint.app

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moodprint.app.domain.ActionCatalog
import com.moodprint.app.domain.AnimalKind
import com.moodprint.app.ui.action.ActionExecutionContent
import com.moodprint.app.ui.checkin.CheckInContent
import com.moodprint.app.ui.designsystem.MoodprintColors
import com.moodprint.app.ui.designsystem.MoodprintDesignTheme
import com.moodprint.app.ui.home.MoodprintMainScreen
import com.moodprint.app.ui.model.ActionChangeUi
import com.moodprint.app.ui.model.toUiModel
import com.moodprint.app.ui.onboarding.IntroScreen
import com.moodprint.app.ui.onboarding.ProfileScreen
import com.moodprint.app.ui.onboarding.WelcomeScreen
import com.moodprint.app.ui.recommendation.RecommendationContent
import com.moodprint.app.ui.reward.RewardScreen

enum class Screen { Welcome, Intro, Profile, Home, CheckIn, Recommend, Action, Reward }
enum class MainTab { Home, Collection, Records }

data class MoodLog(
    val id: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val recordedLocalDate: String = "",
    val emotions: List<String>,
    val energy: String,
    val note: String,
    val actionTitle: String? = null,
    val actionDetailPrompt: String? = null,
    val actionDetailNote: String? = null,
    val changeLabel: String? = null,
)

private val emotions = listOf("불안", "무기력", "속상함", "화남", "외로움", "복잡함", "지침", "답답함", "슬픔")
private fun actionIcon(symbol: String) = when (symbol) {
    "headphones" -> Icons.Default.Headphones; "wind" -> Icons.Default.Air
    "figure.cooldown" -> Icons.Default.SelfImprovement; "pencil.line" -> Icons.Default.EditNote
    "mug" -> Icons.Default.LocalCafe; "nose" -> Icons.Default.Spa; "drop.fill" -> Icons.Default.WaterDrop
    "eye.fill" -> Icons.Default.Visibility; "moon.zzz.fill" -> Icons.Default.Bedtime
    "iphone.slash" -> Icons.Default.PhoneDisabled; "lightbulb.fill" -> Icons.Default.Lightbulb
    "text.cursor" -> Icons.Default.TextFields; "checkmark.seal.fill" -> Icons.Default.CheckCircle
    "envelope.fill" -> Icons.Default.Mail; "photo.fill" -> Icons.Default.Photo; "heart.fill" -> Icons.Default.Favorite
    "figure.stand" -> Icons.Default.AccessibilityNew; "figure.walk" -> Icons.AutoMirrored.Filled.DirectionsWalk
    "square.grid.3x3.fill" -> Icons.Default.GridView; "camera.fill" -> Icons.Default.CameraAlt
    else -> Icons.Default.Spa
}
private val actions = ActionCatalog.actions.map { action -> action.toUiModel(actionIcon(action.symbolName)) }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MoodprintDesignTheme { MoodprintApp() } }
    }
}

@Composable
fun MoodprintApp(viewModel: MoodprintViewModel = viewModel()) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val pets by viewModel.pets.collectAsStateWithLifecycle()
    val insight by viewModel.personalizationInsight.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val saveState by viewModel.saveMoodState.collectAsStateWithLifecycle()
    val finishState by viewModel.finishActionState.collectAsStateWithLifecycle()
    val syncStatusText by viewModel.syncStatusText.collectAsStateWithLifecycle()
    val syncRequiresReconnect by viewModel.syncRequiresReconnect.collectAsStateWithLifecycle()
    val syncInProgress by viewModel.syncInProgress.collectAsStateWithLifecycle()
    val deleteInProgress by viewModel.deleteInProgress.collectAsStateWithLifecycle()
    val dataManagementMessage by viewModel.dataManagementMessage.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    var tab by rememberSaveable { mutableStateOf(MainTab.Home) }
    var selectedEmotions by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var note by rememberSaveable { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }
    var energy by rememberSaveable { mutableStateOf("보통") }
    var recordDateMillis by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedActionId by rememberSaveable { mutableStateOf(actions.first().id) }
    var rewardExperience by rememberSaveable { mutableIntStateOf(15) }
    var rewardFragments by rememberSaveable { mutableIntStateOf(0) }
    var unlockedPetName by rememberSaveable { mutableStateOf<String?>(null) }
    var rewardChange by rememberSaveable { mutableStateOf<ActionChangeUi?>(null) }
    var rewardPetName by rememberSaveable { mutableStateOf(AnimalKind.CAT.koreanName) }
    var rewardPetLevel by rememberSaveable { mutableIntStateOf(1) }
    var rewardPetColorName by rememberSaveable { mutableStateOf(AnimalKind.CAT.storageKey) }
    var unlockedPetColorName by rememberSaveable { mutableStateOf<String?>(null) }
    var rewardDidLevelUp by rememberSaveable { mutableStateOf(false) }
    var operationErrorKind by rememberSaveable { mutableStateOf<DataOperationKind?>(null) }
    var operationErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var handledSaveEvent by rememberSaveable { mutableStateOf<String?>(null) }
    var handledFinishEvent by rememberSaveable { mutableStateOf<String?>(null) }
    val ranked = remember(selectedEmotions, energy, results) { viewModel.recommendations(selectedEmotions, energy) }
    val recommendationCandidates = remember(ranked) { ranked.take(MAX_RECOMMENDATION_CANDIDATES) }
    val experience = pets.firstOrNull { it.isPrimary }?.experience ?: 0
    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route

    LaunchedEffect(profile.loaded, profile.onboardingCompleted, currentRoute) {
        if (shouldEnterHomeAfterProfileLoad(profile.loaded, profile.onboardingCompleted, currentRoute)) nav.navigate(Screen.Home.name) {
            popUpTo(Screen.Welcome.name) { inclusive = true }; launchSingleTop = true
        }
        if (shouldReturnToWelcomeAfterDataReset(profile.loaded, profile.onboardingCompleted, currentRoute)) nav.navigate(Screen.Welcome.name) {
            popUpTo(0) { inclusive = true }; launchSingleTop = true
        }
    }
    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is DataOperation.Success -> if (handledSaveEvent != state.eventId) {
                handledSaveEvent = state.eventId; nav.navigate(Screen.Recommend.name); viewModel.resetSaveMoodState()
            }
            is DataOperation.Failure -> {
                operationErrorKind = state.operation
                operationErrorMessage = state.message
            }
            else -> Unit
        }
    }
    LaunchedEffect(finishState) {
        when (val state = finishState) {
            is DataOperation.Success -> if (handledFinishEvent != state.eventId) {
                handledFinishEvent = state.eventId
                rewardExperience = state.value.reward.experienceAwarded
                rewardFragments = state.value.reward.fragmentsAwarded
                unlockedPetName = state.value.reward.newlyUnlockedPet?.name
                rewardPetName = state.value.reward.primaryPet.name
                rewardPetLevel = state.value.reward.primaryPet.level
                rewardPetColorName = state.value.reward.primaryPet.colorName
                unlockedPetColorName = state.value.reward.newlyUnlockedPet?.colorName
                val previousExperience = (state.value.reward.primaryPet.experience - state.value.reward.experienceAwarded).coerceAtLeast(0)
                rewardDidLevelUp = state.value.reward.primaryPet.level > previousExperience / 100 + 1
                nav.navigate(Screen.Reward.name)
                viewModel.resetFinishActionState()
            }
            is DataOperation.Failure -> {
                operationErrorKind = state.operation
                operationErrorMessage = state.message
            }
            else -> Unit
        }
    }

    Surface(Modifier.fillMaxSize(), color = MoodprintColors.Background) {
        if (!profile.loaded) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MoodprintColors.Primary)
        } else NavHost(nav, startDestination = Screen.Welcome.name) {
            composable(Screen.Welcome.name) { WelcomeScreen(onStart = { nav.navigate(Screen.Intro.name) }) }
            composable(Screen.Intro.name) { IntroScreen(onBack = { nav.popBackStack() }, onContinue = { nav.navigate(Screen.Profile.name) }) }
            composable(Screen.Profile.name) {
                ProfileScreen(nickname, { nickname = it }, { nav.popBackStack() }, onComplete = { viewModel.completeOnboarding(nickname) })
            }
            composable(Screen.Home.name) {
                MoodprintMainScreen(tab, { tab = it }, experience, logs, pets, profile.nickname, viewModel::updateNickname, onCheckIn = { date ->
                    viewModel.beginNewMood(); selectedEmotions = emptyList(); note = ""; energy = "보통"
                    recordDateMillis = date ?: System.currentTimeMillis(); nav.navigate(Screen.CheckIn.name)
                }, insight = insight, syncEnabled = viewModel.syncEnabled, syncStatusText = syncStatusText,
                    syncInProgress = syncInProgress, syncRequiresReconnect = syncRequiresReconnect, deleteInProgress = deleteInProgress,
                    onRetrySync = viewModel::retrySync, onReconnectSync = viewModel::reconnectSync, onDeleteAllData = viewModel::deleteAllDataIncludingServer,
                    onSetPrimaryPet = viewModel::setPrimaryPet)
            }
            composable(Screen.CheckIn.name) {
                CheckInContent(emotions, selectedEmotions, note, energy, { nav.popBackStack() }, { emotion ->
                    selectedEmotions = if (emotion in selectedEmotions) selectedEmotions - emotion
                    else if (selectedEmotions.size < 3) selectedEmotions + emotion else selectedEmotions
                }, { note = it }, { energy = it }, {
                    selectedActionId = ranked.firstOrNull()?.action?.id ?: actions.first().id
                    viewModel.saveMood(selectedEmotions, energy, note, recordDateMillis)
                }, saving = saveState is DataOperation.Loading)
            }
            composable(Screen.Recommend.name) {
                val action = actions.first { it.id == selectedActionId }
                RecommendationContent(action,
                    ranked.firstOrNull { it.action.id == selectedActionId }?.reason
                        ?: "지금 선택한 마음과 에너지에 맞춰 부담이 적은 행동을 준비했어요.",
                    { nav.popBackStack() }, { nav.navigate(Screen.Action.name) }, {
                        selectedActionId = nextRecommendationId(
                            recommendationCandidates.map { it.action.id },
                            selectedActionId,
                        ) ?: selectedActionId
                    },
                    currentPosition = recommendationCandidates.indexOfFirst { it.action.id == selectedActionId }
                        .takeIf { it >= 0 }?.plus(1) ?: 1,
                    candidateCount = recommendationCandidates.size,
                )
            }
            composable(Screen.Action.name) {
                ActionExecutionContent(actions.first { it.id == selectedActionId }, { nav.popBackStack() }, onFinish = { change, detail ->
                    rewardChange = change
                    viewModel.finishAction(selectedActionId, change, detail)
                }, saving = finishState is DataOperation.Loading)
            }
            composable(Screen.Reward.name) {
                BackHandler { nav.navigate(Screen.Home.name) { popUpTo(Screen.Home.name) { inclusive = false }; launchSingleTop = true } }
                RewardScreen(rewardExperience, rewardFragments, unlockedPetName, {
                    nav.navigate(Screen.Home.name) { popUpTo(Screen.Home.name) { inclusive = false }; launchSingleTop = true }
                }, {
                    tab = MainTab.Collection
                    nav.navigate(Screen.Home.name) { popUpTo(Screen.Home.name) { inclusive = false }; launchSingleTop = true }
                }, change = rewardChange, petName = rewardPetName, petLevel = rewardPetLevel,
                    petColorName = rewardPetColorName, unlockedPetColorName = unlockedPetColorName,
                    didLevelUp = rewardDidLevelUp)
            }
        }
    }
    if (saveState is DataOperation.Loading || finishState is DataOperation.Loading) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MoodprintColors.Primary)
        }
    }
    val errorKind = operationErrorKind
    val errorMessage = operationErrorMessage
    if (errorKind != null && errorMessage != null) {
        fun closeError() {
            operationErrorKind = null
            operationErrorMessage = null
        }
        AlertDialog(
            onDismissRequest = { viewModel.dismissFailure(errorKind); closeError() },
            title = { Text(if (errorKind == DataOperationKind.SAVE_MOOD) "마음 기록을 저장하지 못했어요" else "행동 완료를 저장하지 못했어요") },
            text = { Text(errorMessage) },
            dismissButton = { TextButton({ viewModel.dismissFailure(errorKind); closeError() }) { Text("닫기") } },
            confirmButton = { TextButton({ closeError(); viewModel.retry(errorKind) }) { Text("다시 시도") } },
        )
    }
    if (dataManagementMessage != null) AlertDialog(
        onDismissRequest = viewModel::clearDataManagementMessage,
        title = { Text("기록 및 동기화 안내") },
        text = { Text(dataManagementMessage.orEmpty()) },
        confirmButton = { TextButton(viewModel::clearDataManagementMessage) { Text("확인") } },
    )
}

internal const val MAX_RECOMMENDATION_CANDIDATES = 5

internal fun nextRecommendationId(candidateIds: List<String>, currentId: String): String? {
    if (candidateIds.isEmpty()) return null
    val currentIndex = candidateIds.indexOf(currentId)
    return candidateIds[(currentIndex + 1).mod(candidateIds.size)]
}

internal fun shouldEnterHomeAfterProfileLoad(loaded: Boolean, onboardingCompleted: Boolean, currentRoute: String?): Boolean =
    loaded && onboardingCompleted && currentRoute in setOf(Screen.Welcome.name, Screen.Intro.name, Screen.Profile.name)

internal fun shouldReturnToWelcomeAfterDataReset(loaded: Boolean, onboardingCompleted: Boolean, currentRoute: String?): Boolean =
    loaded && !onboardingCompleted && currentRoute != null && currentRoute !in setOf(Screen.Welcome.name, Screen.Intro.name, Screen.Profile.name)
