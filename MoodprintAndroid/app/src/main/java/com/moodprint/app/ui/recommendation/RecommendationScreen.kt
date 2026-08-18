package com.moodprint.app.ui.recommendation

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.moodprint.app.ui.components.MoodprintEyebrow
import com.moodprint.app.ui.components.MoodprintNavigationBackButton
import com.moodprint.app.ui.designsystem.MoodprintPrimaryButton
import com.moodprint.app.ui.designsystem.MoodprintRadius
import com.moodprint.app.ui.designsystem.MoodprintSpacing
import com.moodprint.app.ui.components.MoodprintStatusTag

@Composable
fun RecommendationContent(
    action: RecoveryActionUiModel,
    reason: String,
    onBack: () -> Unit,
    onStart: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    currentPosition: Int = 1,
    candidateCount: Int = 1,
) {
    Column(modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(MoodprintSpacing.XLarge), verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Large)) {
        MoodprintNavigationBackButton(onBack)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                MoodprintEyebrow("오늘의 작은 행동")
                if (candidateCount > 1) {
                    MoodprintStatusTag("추천 $currentPosition/$candidateCount", MoodprintColors.SoftPurple)
                }
            }
            Spacer(Modifier.height(MoodprintSpacing.Small))
            Text("지금 할 수 있는 하나", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))
            ActionCard(action)
            Spacer(Modifier.height(14.dp))
            Column(
                Modifier.fillMaxWidth().background(MoodprintColors.SoftPurple.copy(alpha = 0.62f), RoundedCornerShape(MoodprintRadius.Card)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Small),
            ) {
                MoodprintEyebrow("지금의 마음에 맞춰서")
                Text(reason)
            }
        }
        MoodprintPrimaryButton("이 행동 시작하기", onStart)
        OutlinedButton(onNext, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Icon(Icons.Default.Refresh, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("다른 행동 보기") }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActionCard(action: RecoveryActionUiModel, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MoodprintColors.Surface),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(Modifier.size(56.dp).background(MoodprintColors.Mint, RoundedCornerShape(MoodprintRadius.Control)), contentAlignment = Alignment.Center) { Icon(action.icon, contentDescription = null, tint = MoodprintColors.Primary) }
            Column(Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(action.title, fontWeight = FontWeight.Bold)
                Text(action.instruction, color = MoodprintColors.SecondaryText)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MoodprintSpacing.Small),
                    verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Small),
                ) {
                    MoodprintStatusTag("약 ${maxOf(1, action.durationSeconds / 60)}분", MoodprintColors.Warm)
                    MoodprintStatusTag(action.categoryLabel, MoodprintColors.SoftPurple)
                }
            }
        }
    }
}
