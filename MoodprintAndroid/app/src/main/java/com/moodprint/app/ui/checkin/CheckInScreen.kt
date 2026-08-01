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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
) {
    var showHelp by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier.fillMaxSize().statusBarsPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
        }
        Text("지금 마음을 남겨주세요", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("감정 선택 · 최대 3개", fontWeight = FontWeight.SemiBold)
            Text("${selectedEmotions.size}/3")
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(emotions, key = { it }) { emotion ->
                val selected = emotion in selectedEmotions
                val enabled = selected || selectedEmotions.size < 3
                Surface(
                    onClick = { onToggleEmotion(emotion) },
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics { this.selected = selected },
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                ) {
                    Box(Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                        Text(emotion, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("무슨 일이 있었나요? · 선택", fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { showHelp = true }) { Icon(Icons.Default.Info, contentDescription = "상황 기록 안내") }
                    }
                    OutlinedTextField(note, onNoteChange, Modifier.fillMaxWidth(), minLines = 3, placeholder = { Text("자유롭게 적어보세요.") })
                }
            }
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("현재 에너지", fontWeight = FontWeight.SemiBold)
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        listOf("낮음", "보통", "높음").forEachIndexed { index, value ->
                            SegmentedButton(energy == value, { onEnergyChange(value) }, SegmentedButtonDefaults.itemShape(index, 3)) { Text(value) }
                        }
                    }
                }
            }
        }
        Button(onSubmit, Modifier.fillMaxWidth().heightIn(min = 54.dp), enabled = selectedEmotions.isNotEmpty()) { Text("행동 추천 받기") }
    }
    if (showHelp) AlertDialog(
        onDismissRequest = { showHelp = false },
        confirmButton = { TextButton({ showHelp = false }) { Text("확인") } },
        title = { Text("꼭 적지 않아도 괜찮아요") },
        text = { Text("상황 기록은 선택 사항이에요.") },
    )
}
