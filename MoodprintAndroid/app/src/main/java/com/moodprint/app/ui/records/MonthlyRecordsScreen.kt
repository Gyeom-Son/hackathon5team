package com.moodprint.app.ui.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
import com.moodprint.app.ui.components.MoodprintPet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import com.moodprint.app.MoodLog
import com.moodprint.app.ui.designsystem.MoodprintColors
import com.moodprint.app.ui.designsystem.MoodprintRadius
import com.moodprint.app.ui.designsystem.MoodprintSpacing
import com.moodprint.app.util.MonthlyStatsCalculator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 기록 탭의 "월별" 뷰. 이번 달 기분 통계와, 글이나 행동 기록을 남긴 날을 모아 보여준다.
 * 실제로 저장된 기록만 집계하므로 아무 것도 없으면 그 사실을 그대로 안내한다.
 */
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun MoodprintMonthlyRecords(
    logs: List<MoodLog>,
    monthOffset: Int,
    onMonthOffsetChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val stats = MonthlyStatsCalculator.calculate(logs, today, monthOffset)
    val topEmotionCount = stats.emotionCounts.firstOrNull()?.count
    val topEmotions = stats.emotionCounts
        .filter { it.count == topEmotionCount }
        .map { it.emotion }
    val topEnergyCount = stats.energyCounts.values.maxOrNull()
    val dominantEnergies = stats.energyCounts
        .filterValues { it == topEnergyCount }
        .keys
        .sorted()
    val useStackedMetrics = LocalDensity.current.fontScale >= 1.3f

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Large)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onMonthOffsetChange(monthOffset - 1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "이전 달")
            }
            Text("${stats.year}년 ${stats.month}월", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { onMonthOffsetChange(monthOffset + 1) }, enabled = monthOffset < 0) {
                Icon(Icons.Default.ChevronRight, contentDescription = "다음 달")
            }
        }

        if (stats.checkInCount == 0) {
            Text(
                "이 달엔 아직 기록이 없어요.\n기록하지 않은 달에도 불이익은 없어요.",
                color = MoodprintColors.SecondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = MoodprintSpacing.XLarge),
            )
            return@Column
        }

        Surface(color = MoodprintColors.Surface, shape = RoundedCornerShape(MoodprintRadius.Card)) {
            Column(
                Modifier.padding(MoodprintSpacing.Large),
                verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Medium),
            ) {
                Text("이번 달 기분 통계", fontWeight = FontWeight.Bold)
                MonthlyMetricRow("이번 달 기록", "${stats.checkInCount}회", useStackedMetrics)
                if (topEmotions.isNotEmpty()) {
                    MonthlyMetricRow("자주 기록된 감정", topEmotions.joinToString(" · "), useStackedMetrics)
                }
                if (dominantEnergies.isNotEmpty()) {
                    MonthlyMetricRow("자주 기록된 에너지", dominantEnergies.joinToString(" · "), useStackedMetrics)
                }
                MonthlyMetricRow("완료한 행동", "${stats.actionCompletionCount}개", useStackedMetrics)
                if (stats.emotionCounts.isNotEmpty()) {
                    HorizontalDivider(color = MoodprintColors.Border)
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        stats.emotionCounts.forEach { EmotionCountTag(it.emotion, it.count) }
                    }
                    stats.emotionCounts.take(4).forEach { item ->
                        DistributionRow(item.emotion, item.count, stats.checkInCount.coerceAtLeast(1))
                    }
                }
                if (stats.energyCounts.isNotEmpty()) {
                    Text("에너지 분포", style = MaterialTheme.typography.labelMedium, color = MoodprintColors.SecondaryText)
                    stats.energyCounts.entries.sortedBy { it.key }.forEach { (energy, count) ->
                        DistributionRow(energy, count, stats.checkInCount.coerceAtLeast(1))
                    }
                }
            }
        }

        Surface(color = MoodprintColors.Mint, shape = RoundedCornerShape(MoodprintRadius.Card)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(MoodprintSpacing.Medium)) {
                MoodprintPet(58, happy = false)
                Text("이번 달 기록은 좋고 나쁨을 평가하지 않아요. 남긴 마음을 천천히 돌아볼 수 있어요.", Modifier.weight(1f))
            }
        }

        Text("남긴 글 모아보기", style = MaterialTheme.typography.titleMedium)
        if (stats.moments.isEmpty()) {
            Text("이 달엔 남긴 글이 없어요.", color = MoodprintColors.SecondaryText)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Medium)) {
                stats.moments.forEach { MomentCard(it) }
            }
        }
    }
}

@Composable
private fun MonthlyMetricRow(label: String, value: String, stacked: Boolean) {
    if (stacked) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = MoodprintColors.SecondaryText)
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = MoodprintColors.SecondaryText)
            Text(value, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun DistributionRow(label: String, count: Int, total: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("${count}회", style = MaterialTheme.typography.bodySmall, color = MoodprintColors.SecondaryText)
        }
        LinearProgressIndicator(
            progress = { count.toFloat() / total },
            modifier = Modifier.fillMaxWidth(),
            color = MoodprintColors.Primary,
            trackColor = MoodprintColors.SoftPurple,
        )
    }
}

@Composable
private fun EmotionCountTag(emotion: String, count: Int) {
    Surface(color = MoodprintColors.SoftPurple, shape = RoundedCornerShape(MoodprintRadius.Pill)) {
        Text("$emotion $count", Modifier.padding(horizontal = 11.dp, vertical = 7.dp), maxLines = 1)
    }
}

@Composable
private fun MomentCard(moment: MonthlyStatsCalculator.MonthlyMoment, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MoodprintColors.Surface,
        shape = RoundedCornerShape(MoodprintRadius.Card),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                moment.date.format(DateTimeFormatter.ofPattern("M월 d일 E요일", Locale.KOREA)),
                fontWeight = FontWeight.Bold,
            )
            Text(moment.emotions.joinToString(" · "), color = MoodprintColors.SecondaryText)
            Text("“${moment.text}”")
            if (moment.actionTitle != null) {
                Text(moment.actionTitle, color = MoodprintColors.Primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
