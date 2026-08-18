package com.moodprint.app.ui.records

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moodprint.app.MoodLog
import com.moodprint.app.ui.designsystem.MoodprintColors
import com.moodprint.app.ui.designsystem.MoodprintSpacing
import java.time.ZoneId

private enum class RecordsViewMode { CARD, CALENDAR, MONTHLY }

@Composable
fun MoodprintRecordsScreen(
    logs: List<MoodLog>,
    onCheckIn: (epochMillis: Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var viewModeOrdinal by rememberSaveable { mutableIntStateOf(RecordsViewMode.CARD.ordinal) }
    val viewMode = RecordsViewMode.entries.getOrElse(viewModeOrdinal) { RecordsViewMode.CARD }
    var calendarMonthOffset by rememberSaveable { mutableIntStateOf(0) }
    var selectedDateValue by rememberSaveable { mutableStateOf<String?>(null) }
    val calendarState = MoodprintCalendarState(
        monthOffset = calendarMonthOffset,
        selectedDate = selectedDateValue?.let { runCatching { java.time.LocalDate.parse(it) }.getOrNull() },
    )
    var monthlyOffset by rememberSaveable { mutableIntStateOf(0) }
    // Keep each tab's scroll independently. A single positional remember inside
    // the when branch made another tab open halfway down the page.
    val calendarScrollState = rememberScrollState()
    val monthlyScrollState = rememberScrollState()
    Column(
        modifier = modifier.fillMaxSize().padding(MoodprintSpacing.XLarge),
        verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Large),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("나의 마음 기록", style = MaterialTheme.typography.headlineMedium)
            FilledTonalButton(onClick = { onCheckIn(null) }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.size(5.dp))
                Text("새 기록")
            }
        }
        PrimaryTabRow(selectedTabIndex = viewMode.ordinal, containerColor = MoodprintColors.Background) {
            RecordsViewMode.entries.forEach { mode ->
                Tab(
                    selected = viewMode == mode,
                    onClick = { viewModeOrdinal = mode.ordinal },
                    text = { Text(when (mode) { RecordsViewMode.CARD -> "카드"; RecordsViewMode.CALENDAR -> "캘린더"; RecordsViewMode.MONTHLY -> "월별" }) },
                    selectedContentColor = MoodprintColors.Primary,
                    unselectedContentColor = MoodprintColors.SecondaryText,
                )
            }
        }
        when (viewMode) {
            RecordsViewMode.CALENDAR -> MoodprintCalendarRecords(
                logs = logs,
                state = calendarState,
                onStateChange = {
                    calendarMonthOffset = it.monthOffset
                    selectedDateValue = it.selectedDate?.toString()
                },
                onRecordDate = { date ->
                    onCheckIn(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                },
                modifier = Modifier.weight(1f).verticalScroll(calendarScrollState),
            )
            RecordsViewMode.MONTHLY -> MoodprintMonthlyRecords(
                logs = logs,
                monthOffset = monthlyOffset,
                onMonthOffsetChange = { monthlyOffset = it },
                modifier = Modifier.weight(1f).verticalScroll(monthlyScrollState),
            )
            RecordsViewMode.CARD -> if (logs.isEmpty()) {
                EmptyRecords(Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Large),
                ) {
                    items(logs, key = { it.stableKey }) { log -> MoodprintRecordCard(log) }
                }
            }
        }
    }
}

@Composable
private fun EmptyRecords(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(46.dp), tint = MoodprintColors.Primary)
        Spacer(Modifier.size(MoodprintSpacing.Medium))
        Text("아직 기록이 없어요", fontWeight = FontWeight.Bold)
        Text(
            "기록하지 않은 날에도 불이익은 없어요.\n필요할 때 편하게 마음을 남겨주세요.",
            color = MoodprintColors.SecondaryText,
            textAlign = TextAlign.Center,
        )
    }
}
