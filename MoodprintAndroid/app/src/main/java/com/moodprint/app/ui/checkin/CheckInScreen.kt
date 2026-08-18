package com.moodprint.app.ui.checkin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import com.moodprint.app.ui.components.MoodprintNavigationBackButton
import com.moodprint.app.ui.designsystem.MoodprintChoiceChip
import com.moodprint.app.ui.designsystem.MoodprintColors
import com.moodprint.app.ui.designsystem.MoodprintPrimaryButton
import com.moodprint.app.ui.designsystem.MoodprintRadius
import com.moodprint.app.ui.designsystem.MoodprintSpacing

@Composable
fun CheckInContent(
    emotions: List<String>,
    selectedEmotions: List<String>,
    note: String,
    energy: String,
    onBack: () -> Unit,
    onToggleEmotion: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onEnergyChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    saving: Boolean = false,
) {
    var showHelp by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(MoodprintSpacing.XLarge),
        verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Large),
    ) {
        MoodprintNavigationBackButton(onBack)
        Text("지금 마음을 남겨주세요", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("감정 선택 · 최대 3개", fontWeight = FontWeight.SemiBold)
            Surface(color = MoodprintColors.SoftPurple, shape = RoundedCornerShape(MoodprintRadius.Pill)) {
                Text("${selectedEmotions.size}/3", Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = MoodprintColors.Primary, fontWeight = FontWeight.Bold)
            }
        }
        if (selectedEmotions.size == 3) {
            Text(
                "다른 감정을 고르려면 선택한 감정 하나를 먼저 해제해 주세요.",
                color = MoodprintColors.SecondaryText,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(96.dp),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(emotions, key = { it }) { emotion ->
                val selected = emotion in selectedEmotions
                val enabled = selected || selectedEmotions.size < 3
                MoodprintChoiceChip(
                    label = emotion,
                    selected = selected,
                    onClick = { onToggleEmotion(emotion) },
                    modifier = Modifier.fillMaxWidth().semantics {
                        if (!enabled) stateDescription = "최대 3개를 선택했어요. 선택한 감정을 해제하면 고를 수 있어요."
                    },
                    enabled = enabled,
                )
            }
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Surface(color = MoodprintColors.Surface, shape = RoundedCornerShape(MoodprintRadius.Card)) { Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("무슨 일이 있었나요?", fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { showHelp = true }) { Icon(Icons.Default.Info, contentDescription = "상황 기록 안내") }
                    }
                    OutlinedTextField(
                        note,
                        onNoteChange,
                        Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("자유롭게 적어보세요.") },
                        shape = RoundedCornerShape(MoodprintRadius.Control),
                    )
                } }
            }
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Surface(color = MoodprintColors.Surface, shape = RoundedCornerShape(MoodprintRadius.Card)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("현재 에너지", fontWeight = FontWeight.SemiBold)
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        listOf("낮음", "보통", "높음").forEachIndexed { index, value ->
                            SegmentedButton(energy == value, { onEnergyChange(value) }, SegmentedButtonDefaults.itemShape(index, 3)) { Text(value) }
                        }
                    }
                } }
            }
        }
        MoodprintPrimaryButton(if (saving) "저장 중…" else "행동 추천받기", onSubmit, enabled = selectedEmotions.isNotEmpty() && !saving)
    }
    if (showHelp) AlertDialog(
        onDismissRequest = { showHelp = false },
        confirmButton = { TextButton({ showHelp = false }) { Text("확인") } },
        title = { Text("꼭 적지 않아도 괜찮아요") },
        text = { Text("상황 기록은 선택 사항이에요. 다만 어떤 일이 있었는지 짧게 남겨두면, 나중에 나에게 더 잘 맞는 대처를 찾는 데 도움이 될 수 있어요.") },
    )
}
