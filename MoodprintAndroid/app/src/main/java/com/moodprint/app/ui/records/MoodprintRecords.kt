package com.moodprint.app.ui.records

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moodprint.app.MoodLog
import com.moodprint.app.ui.designsystem.MoodprintColors
import com.moodprint.app.ui.designsystem.MoodprintRadius
import com.moodprint.app.ui.designsystem.MoodprintSpacing
import com.moodprint.app.util.CalendarMonthCalculator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Immutable
data class MoodprintCalendarState(
    val monthOffset: Int = 0,
    val selectedDate: LocalDate? = null,
)

/**
 * 월 이동과 날짜 선택은 외부 state로 관리한다. 선택된 날의 기록 카드를 달력 아래에 표시한다.
 */
@Composable
fun MoodprintCalendarRecords(
    logs: List<MoodLog>,
    state: MoodprintCalendarState,
    onStateChange: (MoodprintCalendarState) -> Unit,
    onRecordDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    zoneId: ZoneId = ZoneId.systemDefault(),
) {
    val dates = logs.map { Instant.ofEpochMilli(it.createdAt).atZone(zoneId).toLocalDate() }
    val month = CalendarMonthCalculator.calculate(today, state.monthOffset, dates)
    val logsByDate = logs.groupBy {
        Instant.ofEpochMilli(it.createdAt).atZone(zoneId).toLocalDate()
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Medium)) {
        Surface(
            color = MoodprintColors.Surface,
            shape = RoundedCornerShape(MoodprintRadius.Card),
        ) {
            Column(
                Modifier.padding(MoodprintSpacing.Large),
                verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Small),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = {
                        onStateChange(state.copy(monthOffset = state.monthOffset - 1, selectedDate = null))
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "이전 달")
                    }
                    Text("${month.year}년 ${month.month}월", style = MaterialTheme.typography.titleMedium)
                    IconButton(
                        onClick = {
                            onStateChange(state.copy(monthOffset = state.monthOffset + 1, selectedDate = null))
                        },
                        enabled = state.monthOffset < 0,
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "다음 달")
                    }
                }
                CalendarWeekHeader()
                repeat(6) { week ->
                    Row(Modifier.fillMaxWidth()) {
                        repeat(7) { weekday ->
                            val number = week * 7 + weekday - month.firstWeekday + 1
                            val date = if (number in 1..month.daysInMonth) {
                                LocalDate.of(month.year, month.month, number)
                            } else null
                            CalendarDay(
                                date = date,
                                isToday = date == today,
                                enabled = date == null || !date.isAfter(today),
                                isSelected = date == state.selectedDate,
                                hasRecord = number in month.recordedDays,
                                onSelect = { selected ->
                                    onStateChange(state.copy(selectedDate = selected))
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        CalendarLegend()
        state.selectedDate?.let { selectedDate ->
            val selectedLogs = logsByDate[selectedDate].orEmpty()
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("M월 d일 E요일", Locale.KOREA)),
                style = MaterialTheme.typography.titleMedium,
            )
            if (selectedLogs.isEmpty()) {
                Text("이날은 남긴 기록이 없어요.", color = MoodprintColors.SecondaryText)
            } else {
                selectedLogs.forEach { MoodprintRecordCard(it) }
            }
            Button(
                onClick = { onRecordDate(selectedDate) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !selectedDate.isAfter(today),
                shape = RoundedCornerShape(MoodprintRadius.Control),
            ) { Text("이 날짜에 기록 남기기") }
        }
    }
}

@Composable
fun MoodprintRecordCard(log: MoodLog, modifier: Modifier = Modifier) {
    val time = Instant.ofEpochMilli(log.createdAt)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MoodprintColors.Surface,
        shape = RoundedCornerShape(MoodprintRadius.Card),
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Medium),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    Instant.ofEpochMilli(log.createdAt).atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("M월 d일 E요일", Locale.KOREA)),
                    fontWeight = FontWeight.Bold,
                )
                Text(time, color = MoodprintColors.SecondaryText)
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                log.emotions.forEach { RecordTag(it, MoodprintColors.SoftPurple) }
                RecordTag("에너지 ${log.energy}", MoodprintColors.Mint)
            }
            if (log.note.isNotBlank()) Text("“${log.note}”")
            if (log.actionTitle != null) {
                HorizontalDivider(color = MoodprintColors.Border)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MoodprintColors.Primary)
                    Spacer(Modifier.size(10.dp))
                    Column {
                        Text("실행한 행동", color = MoodprintColors.SecondaryText, style = MaterialTheme.typography.labelSmall)
                        Text(log.actionTitle, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (!log.actionDetailNote.isNullOrBlank()) {
                    Text(log.actionDetailPrompt ?: "행동하며 남긴 기록", color = MoodprintColors.SecondaryText)
                    Text(log.actionDetailNote, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    log.changeLabel?.let { "행동 후 변화 · $it" } ?: "행동을 완료했어요",
                    color = MoodprintColors.Primary,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                Text("마음만 기록했어요", color = MoodprintColors.SecondaryText)
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate?,
    isToday: Boolean,
    enabled: Boolean,
    isSelected: Boolean,
    hasRecord: Boolean,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = date?.let {
        "${it.monthValue}월 ${it.dayOfMonth}일" +
            (if (isToday) ", 오늘" else "") +
            (if (!enabled) ", 미래 날짜, 선택할 수 없음" else "") +
            (if (hasRecord) ", 기록 있음" else "") +
            (if (isSelected) ", 선택됨" else "")
    }
    Box(
        modifier = modifier
            .height(42.dp)
            .then(if (date != null && enabled) Modifier.clickable(role = Role.Button) { onSelect(date) } else Modifier)
            .then(if (description != null) Modifier.semantics {
                contentDescription = description
                selected = isSelected
                role = Role.Button
            } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (date != null) {
            Box(
                Modifier
                    .size(34.dp)
                    .background(
                        when {
                            isSelected -> MoodprintColors.Primary
                            hasRecord -> MoodprintColors.SoftPurple
                            else -> Color.Transparent
                        },
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    date.dayOfMonth.toString(),
                    color = if (!enabled) MoodprintColors.SecondaryText.copy(alpha = 0.45f) else if (isSelected) Color.White else MoodprintColors.Ink,
                    textAlign = TextAlign.Center,
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
            if (isToday && !isSelected) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .size(4.dp)
                        .background(MoodprintColors.Primary, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun CalendarWeekHeader() {
    Row(Modifier.fillMaxWidth()) {
        listOf("일", "월", "화", "수", "목", "금", "토").forEach {
            Text(
                it,
                Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MoodprintColors.SecondaryText,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun CalendarLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(MoodprintSpacing.Large)) {
        LegendItem(MoodprintColors.SoftPurple, "기록 있음")
        LegendItem(MoodprintColors.Primary, "선택")
        LegendItem(Color.Transparent, "오늘", showDot = true)
    }
}

@Composable
private fun LegendItem(color: Color, label: String, showDot: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(12.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (showDot) Box(Modifier.size(4.dp).background(MoodprintColors.Primary, CircleShape))
        }
        Spacer(Modifier.size(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MoodprintColors.SecondaryText)
    }
}

@Composable
private fun RecordTag(label: String, color: Color) {
    Surface(color = color, shape = RoundedCornerShape(MoodprintRadius.Pill)) {
        Text(label, Modifier.padding(horizontal = 11.dp, vertical = 7.dp), maxLines = 1)
    }
}
