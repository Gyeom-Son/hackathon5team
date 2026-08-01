package com.moodprint.app

import android.os.Bundle
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
import androidx.navigation.compose.rememberNavController
import com.moodprint.app.domain.ActionCatalog
import com.moodprint.app.ui.action.ActionExecutionContent
import com.moodprint.app.ui.checkin.CheckInContent
import com.moodprint.app.ui.designsystem.MoodprintColors
import com.moodprint.app.ui.designsystem.MoodprintDesignTheme
import com.moodprint.app.ui.home.MoodprintMainScreen
import com.moodprint.app.ui.model.toUiModel
import com.moodprint.app.ui.onboarding.IntroScreen
import com.moodprint.app.ui.onboarding.ProfileScreen
import com.moodprint.app.ui.onboarding.WelcomeScreen
import com.moodprint.app.ui.recommendation.RecommendationContent
import com.moodprint.app.ui.reward.RewardScreen

enum class Screen { Welcome, Intro, Profile, Home, CheckIn, Recommend, Action, Reward }
enum class MainTab { Home, Collection, Records }

data class MoodLog(
    val createdAt: Long = System.currentTimeMillis(),
    val emotions: List<String>,
    val energy: String,
    val note: String,
    val actionTitle: String? = null,
    val actionDetailPrompt: String? = null,
    val actionDetailNote: String? = null,
    val changeLabel: String? = null,
)

private val emotions = listOf("불안", "무기력", "속상함", "화남", "외로움", "복잡함", "지침", "답답함", "슬픔")
private val actionIcons = listOf(
    Icons.Default.Headphones, Icons.Default.Air, Icons.Default.SelfImprovement, Icons.Default.EditNote,
    Icons.Default.LocalCafe, Icons.Default.Spa, Icons.Default.WaterDrop, Icons.Default.Visibility,
    Icons.Default.Bedtime, Icons.Default.PhoneDisabled, Icons.Default.Lightbulb, Icons.Default.TextFields,
    Icons.Default.CheckCircle, Icons.Default.Mail, Icons.Default.Photo, Icons.Default.Favorite,
    Icons.Default.AccessibilityNew, Icons.AutoMirrored.Filled.DirectionsWalk, Icons.Default.GridView,
    Icons.Default.CameraAlt,
)
private val actions = ActionCatalog.actions.mapIndexed { index, action -> action.toUiModel(actionIcons[index]) }

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
    val results by viewModel.results.collectAsStateWithLifecycle()
    val saveState by viewModel.saveMoodState.collectAsStateWithLifecycle()
    val finishState by viewModel.finishActionState.collectAsStateWithLifecycle()
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
    var operationError by rememberSaveable { mutableStateOf<String?>(null) }
    val ranked = remember(selectedEmotions, energy, results) { viewModel.recommendations(selectedEmotions, energy) }
    val experience = pets.firstOrNull { it.isPrimary }?.experience ?: 0

    LaunchedEffect(profile.loaded, profile.onboardingCompleted) {
        if (profile.loaded && profile.onboardingCompleted) nav.navigate(Screen.Home.name) {
            popUpTo(Screen.Welcome.name) { inclusive = true }; launchSingleTop = true
        }
    }
    LaunchedEffect(saveState) {
        when (val state = saveState) {
            is DataOperation.Success -> { nav.navigate(Screen.Recommend.name); viewModel.resetSaveMoodState() }
            is DataOperation.Failure -> operationError = state.message
            else -> Unit
        }
    }
    LaunchedEffect(finishState) {
        when (val state = finishState) {
            is DataOperation.Success -> {
                rewardExperience = state.value.reward.experienceAwarded
                rewardFragments = state.value.reward.fragmentsAwarded
                unlockedPetName = state.value.reward.newlyUnlockedPet?.name
                nav.navigate(Screen.Reward.name)
                viewModel.resetFinishActionState()
            }
            is DataOperation.Failure -> operationError = state.message
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
                    viewModel.beginNewMood(); selectedEmotions = emptyList(); note = ""
                    recordDateMillis = date ?: System.currentTimeMillis(); nav.navigate(Screen.CheckIn.name)
                })
            }
            composable(Screen.CheckIn.name) {
                CheckInContent(emotions, selectedEmotions, note, energy, { nav.popBackStack() }, { emotion ->
                    selectedEmotions = if (emotion in selectedEmotions) selectedEmotions - emotion
                    else if (selectedEmotions.size < 3) selectedEmotions + emotion else selectedEmotions
                }, { note = it }, { energy = it }, {
                    selectedActionId = ranked.firstOrNull()?.action?.id ?: actions.first().id
                    viewModel.saveMood(selectedEmotions, energy, note, recordDateMillis)
                })
            }
            composable(Screen.Recommend.name) {
                val action = actions.first { it.id == selectedActionId }
                RecommendationContent(action,
                    ranked.firstOrNull { it.action.id == selectedActionId }?.reason
                        ?: "지금 선택한 마음과 에너지에 맞춰 부담이 적은 행동을 준비했어요.",
                    { nav.popBackStack() }, { nav.navigate(Screen.Action.name) }, {
                        val index = ranked.indexOfFirst { it.action.id == selectedActionId }
                        if (ranked.isNotEmpty()) selectedActionId = ranked[(index + 1).mod(ranked.size)].action.id
                    })
            }
            composable(Screen.Action.name) {
                ActionExecutionContent(actions.first { it.id == selectedActionId }, { nav.popBackStack() }, onFinish = { change, detail ->
                    viewModel.finishAction(selectedActionId, change, detail)
                })
            }
            composable(Screen.Reward.name) {
                RewardScreen(rewardExperience, rewardFragments, unlockedPetName, {
                    nav.navigate(Screen.Home.name) { popUpTo(Screen.Home.name) { inclusive = false }; launchSingleTop = true }
                }, {
                    tab = MainTab.Collection
                    nav.navigate(Screen.Home.name) { popUpTo(Screen.Home.name) { inclusive = false }; launchSingleTop = true }
                })
            }
        }
    }
    if (saveState is DataOperation.Loading || finishState is DataOperation.Loading) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MoodprintColors.Primary)
        }
    }
    operationError?.let { message ->
        AlertDialog(onDismissRequest = {}, title = { Text("저장하지 못했어요") }, text = { Text(message) },
            confirmButton = { TextButton({ operationError = null; viewModel.resetSaveMoodState(); viewModel.resetFinishActionState() }) { Text("확인") } })
    }
}
