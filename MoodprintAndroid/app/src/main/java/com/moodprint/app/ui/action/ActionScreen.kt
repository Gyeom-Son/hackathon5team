package com.moodprint.app.ui.action

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moodprint.app.ui.model.ActionChangeUi
import com.moodprint.app.ui.model.RecoveryActionUiModel
import com.moodprint.app.util.ActionTimerState
import kotlinx.coroutines.delay
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.moodprint.app.ui.components.MoodprintNavigationBackButton
import com.moodprint.app.ui.designsystem.MoodprintChoiceChip
import com.moodprint.app.ui.designsystem.MoodprintColors
import com.moodprint.app.ui.designsystem.MoodprintPrimaryButton
import com.moodprint.app.ui.designsystem.MoodprintRadius
import com.moodprint.app.ui.designsystem.MoodprintSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionExecutionContent(
    action: RecoveryActionUiModel,
    onBack: () -> Unit,
    onFinish: (ActionChangeUi, String?) -> Unit,
    modifier: Modifier = Modifier,
    saving: Boolean = false,
) {
    val duration = action.durationSeconds * 1_000L
    var timer by rememberSaveable(action.id, stateSaver = actionTimerSaver) {
        mutableStateOf(ActionTimerState(duration))
    }
    var hasStarted by rememberSaveable(action.id) { mutableStateOf(false) }
    var showChangeSheet by rememberSaveable(action.id) { mutableStateOf(false) }
    var showExitConfirmation by rememberSaveable(action.id) { mutableStateOf(false) }
    var detailNote by rememberSaveable(action.id) { mutableStateOf("") }
    var timerAnnouncement by rememberSaveable(action.id) { mutableStateOf("") }
    LaunchedEffect(timer.runningUntilEpochMillis, hasStarted) {
        if (!hasStarted) return@LaunchedEffect
        while (timer.isRunning) {
            timer = timer.snapshot(System.currentTimeMillis())
            if (!timer.isRunning) showChangeSheet = true else delay(250)
        }
    }
    val seconds = ((timer.remainingMillis + 999) / 1_000).toInt()
    BackHandler(enabled = hasStarted && !showChangeSheet) {
        showExitConfirmation = true
    }
    Column(modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(MoodprintSpacing.XLarge), verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Large)) {
        MoodprintNavigationBackButton(onBack = {
            if (hasStarted) showExitConfirmation = true else onBack()
        })
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(action.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Surface(color = MoodprintColors.SoftPurple, shape = RoundedCornerShape(MoodprintRadius.Card)) {
                Text(action.instruction, Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center, color = MoodprintColors.SecondaryText)
            }
            Spacer(Modifier.height(20.dp))
            if (!hasStarted) {
                Text("준비가 되면 타이머를 시작해 보세요.", color = MoodprintColors.SecondaryText)
                action.detailPrompt?.let { prompt ->
                    OutlinedTextField(detailNote, { detailNote = it }, Modifier.fillMaxWidth(), label = { Text("$prompt (선택)") }, placeholder = { Text(action.detailPlaceholder ?: "짧게 남겨보세요") }, shape = RoundedCornerShape(MoodprintRadius.Control), enabled = !saving)
                }
            } else {
                Box(Modifier.size(184.dp).clearAndSetSemantics { contentDescription = "남은 시간 ${seconds / 60}분 ${seconds % 60}초" }, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator({ timer.remainingMillis.toFloat() / duration }, Modifier.fillMaxSize(), strokeWidth = 10.dp)
                    Text(String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                }
                if (seconds == 60 || seconds == 30 || seconds == 0) {
                    Text(
                        if (seconds == 0) "행동 시간이 끝났어요" else "${seconds}초 남았어요",
                        Modifier.clearAndSetSemantics { liveRegion = LiveRegionMode.Polite; contentDescription = if (seconds == 0) "행동 시간이 끝났어요" else "${seconds}초 남았어요" },
                        color = MoodprintColors.SecondaryText,
                    )
                }
                if (timerAnnouncement.isNotEmpty()) {
                    Text(
                        timerAnnouncement,
                        Modifier.clearAndSetSemantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = timerAnnouncement
                        },
                        color = MoodprintColors.SecondaryText,
                    )
                }
                Text("화면을 보지 않아도 괜찮아요.", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
        if (!hasStarted) {
            MoodprintPrimaryButton("타이머 시작하기", {
                hasStarted = true
                timer = timer.start(System.currentTimeMillis())
            }, enabled = !saving)
        } else if (seconds == 0) {
            MoodprintPrimaryButton("변화 기록 이어가기", { showChangeSheet = true }, enabled = !saving)
        } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton({
                if (timer.isRunning) {
                    timer = timer.pause(System.currentTimeMillis())
                    timerAnnouncement = "타이머가 일시정지됐어요."
                } else {
                    timer = timer.start(System.currentTimeMillis())
                    timerAnnouncement = "타이머를 다시 시작했어요."
                }
            }, Modifier.weight(1f).heightIn(min = 54.dp), enabled = !saving) { Text(if (timer.isRunning) "일시정지" else "계속하기") }
            Button({ timer = timer.pause(System.currentTimeMillis()); showChangeSheet = true }, Modifier.weight(1f).heightIn(min = 54.dp), enabled = !saving) { Text(if (saving) "저장 중…" else "완료") }
        }
    }
    if (showExitConfirmation) AlertDialog(
        onDismissRequest = { showExitConfirmation = false },
        title = { Text("행동을 그만할까요?") },
        text = { Text("진행 중인 타이머가 종료돼요. 원하면 계속 진행할 수 있어요.") },
        confirmButton = { TextButton({ showExitConfirmation = false; onBack() }) { Text("그만하기") } },
        dismissButton = { TextButton({ showExitConfirmation = false }) { Text("계속하기") } },
    )
    if (showChangeSheet) ModalBottomSheet(
        onDismissRequest = { if (!saving) showChangeSheet = false },
    ) {
        ChangeSheetContent(onSave = { change ->
            showChangeSheet = false
            onFinish(change, detailNote.trim().ifEmpty { null })
        }, saving = saving)
    }
}

private val actionTimerSaver = listSaver<ActionTimerState, Long>(
    save = { timer ->
        listOf(timer.durationMillis, timer.remainingMillis, timer.runningUntilEpochMillis ?: -1L)
    },
    restore = { values ->
        ActionTimerState(
            durationMillis = values[0],
            remainingMillis = values[1],
            runningUntilEpochMillis = values[2].takeUnless { it == -1L },
        )
    },
)

@Composable
fun ChangeSheetContent(onSave: (ActionChangeUi) -> Unit, saving: Boolean = false) {
    var selected by rememberSaveable { mutableStateOf<ActionChangeUi?>(null) }
    val useSingleColumn = LocalDensity.current.fontScale >= 1.3f
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().imePadding().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("행동 전보다 지금은 어떤가요?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("기분이 나아지지 않아도 괜찮고, 기록을 건너뛰어도 펫은 똑같이 성장해요.")
        ActionChangeUi.entries.filterNot { it == ActionChangeUi.SKIP }.chunked(if (useSingleColumn) 1 else 2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { change ->
                    val icon = when (change) {
                        ActionChangeUi.HARDER -> Icons.Default.Cloud
                        ActionChangeUi.SAME -> Icons.Default.Equalizer
                        ActionChangeUi.BETTER -> Icons.Default.WbSunny
                        ActionChangeUi.MUCH_BETTER -> Icons.Default.LightMode
                        ActionChangeUi.SKIP -> null
                    }
                    MoodprintChoiceChip(change.label, selected == change, { selected = change }, Modifier.weight(1f), enabled = !saving, leadingIcon = icon)
                }
            }
        }
        MoodprintPrimaryButton(if (saving) "저장 중…" else "변화 기록하기", { selected?.let(onSave) }, enabled = selected != null && !saving)
        TextButton({ onSave(ActionChangeUi.SKIP) }, Modifier.fillMaxWidth().heightIn(min = 48.dp), enabled = !saving) { Text("지금은 기록하지 않을래요") }
    }
}
