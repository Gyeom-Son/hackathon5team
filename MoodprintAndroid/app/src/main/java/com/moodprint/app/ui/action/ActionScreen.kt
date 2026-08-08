package com.moodprint.app.ui.action

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moodprint.app.ui.model.ActionChangeUi
import com.moodprint.app.ui.model.RecoveryActionUiModel
import com.moodprint.app.util.ActionTimerState
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionExecutionContent(
    action: RecoveryActionUiModel,
    onBack: () -> Unit,
    onFinish: (ActionChangeUi, String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val duration = action.durationSeconds * 1_000L
    var timer by rememberSaveable(action.id, stateSaver = actionTimerSaver) {
        mutableStateOf(ActionTimerState(duration).start(System.currentTimeMillis()))
    }
    var showChangeSheet by rememberSaveable(action.id) { mutableStateOf(false) }
    var detailNote by rememberSaveable(action.id) { mutableStateOf("") }
    LaunchedEffect(timer.runningUntilEpochMillis) {
        while (timer.isRunning) {
            timer = timer.snapshot(System.currentTimeMillis())
            if (!timer.isRunning) showChangeSheet = true else delay(250)
        }
    }
    val seconds = ((timer.remainingMillis + 999) / 1_000).toInt()
    Column(modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        IconButton(onBack, Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기") }
        Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(action.title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))
            Box(Modifier.size(190.dp).semantics { contentDescription = "남은 시간 ${seconds / 60}분 ${seconds % 60}초" }, contentAlignment = Alignment.Center) {
                CircularProgressIndicator({ timer.remainingMillis.toFloat() / duration }, Modifier.fillMaxSize(), strokeWidth = 10.dp)
                Text(String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            }
            Text("화면을 보지 않아도 괜찮아요.", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            action.detailPrompt?.let { prompt ->
                OutlinedTextField(detailNote, { detailNote = it }, Modifier.fillMaxWidth(), label = { Text(prompt) }, placeholder = { Text(action.detailPlaceholder ?: "짧게 남겨보세요") })
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton({ timer = if (timer.isRunning) timer.pause(System.currentTimeMillis()) else timer.start(System.currentTimeMillis()) }, Modifier.weight(1f).heightIn(min = 54.dp)) { Text(if (timer.isRunning) "일시정지" else "계속하기") }
            Button({ timer = timer.pause(System.currentTimeMillis()); showChangeSheet = true }, Modifier.weight(1f).heightIn(min = 54.dp)) { Text("완료") }
        }
    }
    if (showChangeSheet) ModalBottomSheet(
        onDismissRequest = { showChangeSheet = false; onFinish(ActionChangeUi.SKIP, detailNote.trim().ifEmpty { null }) },
    ) {
        ChangeSheetContent { change ->
            showChangeSheet = false
            onFinish(change, detailNote.trim().ifEmpty { null })
        }
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
fun ChangeSheetContent(onSave: (ActionChangeUi) -> Unit) {
    var selected by rememberSaveable { mutableStateOf<ActionChangeUi?>(null) }
    Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("행동 전보다 지금은 어떤가요?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("기록을 건너뛰어도 펫은 똑같이 성장해요.")
        ActionChangeUi.entries.filterNot { it == ActionChangeUi.SKIP }.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { change ->
                    FilterChip(selected == change, { selected = change }, { Text(change.label) }, Modifier.weight(1f).heightIn(min = 48.dp))
                }
            }
        }
        Button({ selected?.let(onSave) }, Modifier.fillMaxWidth().heightIn(min = 54.dp), enabled = selected != null) { Text("변화 기록하기") }
        TextButton({ onSave(ActionChangeUi.SKIP) }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("지금은 기록하지 않을래요") }
    }
}
