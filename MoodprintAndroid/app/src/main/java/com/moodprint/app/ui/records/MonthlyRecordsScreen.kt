package com.moodprint.app.ui.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
fun MoodprintMonthlyRecords(
    logs: List<MoodLog>,
    monthOffset: Int,
    onMonthOffsetChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val stats = MonthlyStatsCalculator.calculate(logs, today, monthOffset)

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
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("이번 달 기록", color = MoodprintColors.SecondaryText)
                    Text("${stats.checkInCount}회", fontWeight = FontWeight.SemiBold)
                }
                if (stats.topEmotion != null) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("가장 자주 느낀 감정", color = MoodprintColors.SecondaryText)
                        Text(stats.topEmotion, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (stats.dominantEnergy != null) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("가장 많았던 에너지", color = MoodprintColors.SecondaryText)
                        Text(stats.dominantEnergy, fontWeight = FontWeight.SemiBold)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("완료한 행동", color = MoodprintColors.SecondaryText)
                    Text("${stats.actionCompletionCount}개", fontWeight = FontWeight.SemiBold)
                }
                if (stats.emotionCounts.isNotEmpty()) {
                    HorizontalDivider(color = MoodprintColors.Border)
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        stats.emotionCounts.forEach { EmotionCountTag(it.emotion, it.count) }
                    }
                }
            }
        }

        Text("있었던 일 모아보기", style = MaterialTheme.typography.titleMedium)
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    moment.date.format(DateTimeFormatter.ofPattern("M월 d일 E요일", Locale.KOREA)),
                    fontWeight = FontWeight.Bold,
                )
                Text(moment.emotions.joinToString(" · "), color = MoodprintColors.SecondaryText)
            }
            Text("“${moment.text}”")
            if (moment.actionTitle != null) {
                Text(moment.actionTitle, color = MoodprintColors.Primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
