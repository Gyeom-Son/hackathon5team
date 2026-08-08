package com.moodprint.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moodprint.app.MainTab
import com.moodprint.app.MoodLog
import com.moodprint.app.data.local.PetProgressEntity
import com.moodprint.app.domain.PersonalizationInsight
import com.moodprint.app.ui.collection.MoodprintCollectionScreen
import com.moodprint.app.ui.components.MoodprintPet
import com.moodprint.app.ui.designsystem.MoodprintColors
import com.moodprint.app.ui.designsystem.MoodprintPrimaryButton
import com.moodprint.app.ui.designsystem.MoodprintRadius
import com.moodprint.app.ui.designsystem.MoodprintSpacing
import com.moodprint.app.ui.designsystem.petTintFor
import com.moodprint.app.ui.records.MoodprintRecordsScreen

@Composable
fun MoodprintMainScreen(
    tab: MainTab,
    onTabChange: (MainTab) -> Unit,
    experience: Int,
    logs: List<MoodLog>,
    pets: List<PetProgressEntity>,
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onCheckIn: (epochMillis: Long?) -> Unit,
    modifier: Modifier = Modifier,
    insight: PersonalizationInsight? = null,
) {
    var showProfile by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        containerColor = MoodprintColors.Background,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == MainTab.Home,
                    onClick = { onTabChange(MainTab.Home) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("홈") },
                )
                NavigationBarItem(
                    selected = tab == MainTab.Collection,
                    onClick = { onTabChange(MainTab.Collection) },
                    icon = { Icon(Icons.Default.Pets, contentDescription = null) },
                    label = { Text("도감") },
                )
                NavigationBarItem(
                    selected = tab == MainTab.Records,
                    onClick = { onTabChange(MainTab.Records) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("기록") },
                )
            }
        },
    ) { padding ->
        when (tab) {
            MainTab.Home -> MoodprintHomeContent(
                experience = experience,
                logs = logs,
                pets = pets,
                onProfile = { showProfile = true },
                onCheckIn = { onCheckIn(null) },
                modifier = Modifier.padding(padding),
                nickname = nickname,
                insight = insight,
            )
            MainTab.Collection -> MoodprintCollectionScreen(
                pets = pets,
                modifier = Modifier.padding(padding),
            )
            MainTab.Records -> MoodprintRecordsScreen(
                logs = logs,
                onCheckIn = onCheckIn,
                modifier = Modifier.padding(padding),
            )
        }
    }
    if (showProfile) {
        MoodprintProfileDialog(
            currentNickname = nickname,
            onSave = onNicknameChange,
            onDismiss = { showProfile = false },
        )
    }
}

@Composable
fun MoodprintHomeContent(
    experience: Int,
    logs: List<MoodLog>,
    pets: List<PetProgressEntity>,
    onProfile: () -> Unit,
    onCheckIn: () -> Unit,
    modifier: Modifier = Modifier,
    nickname: String = "",
    insight: PersonalizationInsight? = null,
) {
    val primaryPet = pets.firstOrNull { it.isPrimary }
    val progress = (experience.mod(100)) / 100f
    Column(
        modifier = modifier.fillMaxSize().padding(MoodprintSpacing.XLarge),
        verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Large),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("오늘의 동반자", style = MaterialTheme.typography.labelSmall, color = MoodprintColors.Primary)
                Text(
                    "${primaryPet?.name ?: "몽실이"} · ${primaryPet?.level ?: 1}단계",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = MoodprintColors.SoftPurple, shape = RoundedCornerShape(MoodprintRadius.Pill)) {
                    Text(
                        "도감 ${pets.count { it.isUnlocked }}/${pets.size.coerceAtLeast(4)}",
                        Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
                IconButton(onClick = onProfile) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "프로필과 저장 안내", tint = MoodprintColors.Primary)
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MoodprintColors.Surface,
            shape = RoundedCornerShape(MoodprintRadius.Card),
        ) {
            Column(
                Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Medium),
            ) {
                MoodprintPet(
                    size = 112,
                    happy = true,
                    tint = petTintFor(primaryPet?.colorName ?: "lavender"),
                    name = primaryPet?.name ?: "몽실이",
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("다음 성장까지", color = MoodprintColors.SecondaryText)
                    Text("${experience.mod(100)}%", color = MoodprintColors.SecondaryText)
                }
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }
        }
        Text("오늘 마음은 어때요?", style = MaterialTheme.typography.headlineMedium)
        MoodprintPrimaryButton("지금 마음 기록하기", onClick = onCheckIn)
        SupportCard(logs.firstOrNull())
        insight?.let { PersonalizationInsightCard(nickname, it) }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
fun MoodprintProfileDialog(
    currentNickname: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var value by remember(currentNickname) { mutableStateOf(currentNickname) }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(value); onDismiss() }) { Text("완료") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
        title = { Text("${currentNickname.ifBlank { "마음 여행자" }}님의 Moodprint") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Medium)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it.take(12) },
                    label = { Text("닉네임") },
                    singleLine = true,
                )
                Text("로그인 없이 사용 중", color = MoodprintColors.Primary)
                Text(
                    "모든 기록은 이 기기에만 저장돼요. 앱을 삭제하면 기록도 함께 삭제돼요.",
                    color = MoodprintColors.SecondaryText,
                )
            }
        },
    )
}

@Composable
private fun PersonalizationInsightCard(nickname: String, insight: PersonalizationInsight, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MoodprintColors.SoftPurple,
        shape = RoundedCornerShape(MoodprintRadius.Card),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Small)) {
            Text(
                "나에게 맞는 발견",
                style = MaterialTheme.typography.labelSmall,
                color = MoodprintColors.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(insight.message(nickname))
        }
    }
}

@Composable
private fun SupportCard(latestLog: MoodLog?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MoodprintColors.Surface,
        shape = RoundedCornerShape(MoodprintRadius.Card),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Small)) {
            Text(
                if (latestLog == null) "처음이라도 괜찮아요" else "최근 기록",
                style = MaterialTheme.typography.labelSmall,
                color = MoodprintColors.Primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                latestLog?.let { "${it.emotions.joinToString(" · ")} · 에너지 ${it.energy}" }
                    ?: "기록하지 않은 날에도 불이익은 없어요. 필요할 때 시작해 주세요."
            )
        }
    }
}
