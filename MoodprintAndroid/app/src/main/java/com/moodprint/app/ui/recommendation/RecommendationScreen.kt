package com.moodprint.app.ui.recommendation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moodprint.app.ui.model.RecoveryActionUiModel
import com.moodprint.app.ui.designsystem.MoodprintColors

@Composable
fun RecommendationContent(
    action: RecoveryActionUiModel,
    reason: String,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        IconButton(onBack, Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기") }
        Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.Center) {
            Text("지금 할 수 있는 하나", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))
            ActionCard(action)
            Spacer(Modifier.height(14.dp))
            Text(reason, Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(18.dp)).padding(18.dp))
        }
        Button(onStart, Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Text("이 행동 시작하기") }
        OutlinedButton(onNext, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Icon(Icons.Default.Refresh, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("다른 행동 보기") }
    }
}

@Composable
fun ActionCard(action: RecoveryActionUiModel, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MoodprintColors.Surface),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) { Icon(action.icon, contentDescription = null) }
            Column(Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(action.title, fontWeight = FontWeight.Bold)
                Text(action.instruction)
                Text("약 ${maxOf(1, action.durationSeconds / 60)}분 · ${action.categoryLabel}", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
