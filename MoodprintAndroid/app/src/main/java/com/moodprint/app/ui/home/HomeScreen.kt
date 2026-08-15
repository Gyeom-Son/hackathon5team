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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
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
    syncEnabled: Boolean = false,
    syncStatusText: String = if (syncEnabled) "서버 동기화 사용 중" else "이 기기에 저장 중",
    syncInProgress: Boolean = false,
    deleteInProgress: Boolean = false,
    onRetrySync: (() -> Unit)? = null,
    onDeleteAllData: (() -> Unit)? = null,
) {
    var showProfile by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        containerColor = MoodprintColors.Background,
        bottomBar = {
            NavigationBar(containerColor = MoodprintColors.Surface, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = tab == MainTab.Home,
                    onClick = { onTabChange(MainTab.Home) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("홈") },
                    colors = moodprintNavigationColors(),
                )
                NavigationBarItem(
                    selected = tab == MainTab.Collection,
                    onClick = { onTabChange(MainTab.Collection) },
                    icon = { Icon(Icons.Default.Pets, contentDescription = null) },
                    label = { Text("도감") },
                    colors = moodprintNavigationColors(),
                )
                NavigationBarItem(
                    selected = tab == MainTab.Records,
                    onClick = { onTabChange(MainTab.Records) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("기록") },
                    colors = moodprintNavigationColors(),
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
            syncEnabled = syncEnabled,
            syncStatusText = syncStatusText,
            syncInProgress = syncInProgress,
            deleteInProgress = deleteInProgress,
            onRetrySync = onRetrySync,
            onDeleteAllData = onDeleteAllData,
        )
    }
}

@Composable
private fun moodprintNavigationColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MoodprintColors.Ink,
    selectedTextColor = MoodprintColors.Primary,
    indicatorColor = MoodprintColors.SoftPurple,
    unselectedIconColor = MoodprintColors.SecondaryText,
    unselectedTextColor = MoodprintColors.SecondaryText,
)

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
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(MoodprintSpacing.XLarge),
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
            IconButton(onClick = onProfile) {
                Icon(Icons.Default.AccountCircle, contentDescription = "프로필과 저장 안내", tint = MoodprintColors.Primary)
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MoodprintColors.Surface,
            shape = RoundedCornerShape(MoodprintRadius.Card),
        ) {
            Column(
                Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Small),
            ) {
                MoodprintPet(
                    size = 92,
                    happy = true,
                    tint = petTintFor(primaryPet?.colorName ?: "lavender"),
                    name = primaryPet?.name ?: "몽실이",
                    level = primaryPet?.level ?: 1,
                )
                Surface(color = MoodprintColors.SoftPurple, shape = RoundedCornerShape(MoodprintRadius.Pill)) {
                    Text("도감 ${pets.count { it.isUnlocked }}/${pets.size.coerceAtLeast(4)}", Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("다음 성장까지", color = MoodprintColors.SecondaryText)
                    Text("${experience.mod(100)}%", color = MoodprintColors.SecondaryText)
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().semantics {
                        progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
                    },
                )
            }
        }
        Text("오늘 마음은 어때요?", style = MaterialTheme.typography.headlineMedium)
        MoodprintPrimaryButton("지금 마음 기록하기", onClick = onCheckIn)
        SupportCard(logs.firstOrNull())
        insight?.let { PersonalizationInsightCard(nickname, it) }
        if (insight == null) GentleDiscoveryCard(pets)
        Spacer(Modifier.size(MoodprintSpacing.XLarge))
    }
}

@Composable
private fun GentleDiscoveryCard(pets: List<PetProgressEntity>) {
    val next = pets.firstOrNull { !it.isPrimary && !it.isUnlocked }
    Surface(Modifier.fillMaxWidth(), color = MoodprintColors.Mint, shape = RoundedCornerShape(MoodprintRadius.Card)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Small)) {
            Text("천천히 발견해요", style = MaterialTheme.typography.labelSmall, color = MoodprintColors.Primary, fontWeight = FontWeight.Bold)
            Text(next?.let { "${it.name}의 조각 ${it.fragments}/${it.requiredFragments} · 행동을 완료할 때 한 조각씩 만날 수 있어요." }
                ?: "모든 마음 생물을 만났어요. 기록하지 않는 날에도 불이익은 없어요.")
        }
    }
}

@Composable
fun MoodprintProfileDialog(
    currentNickname: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    syncEnabled: Boolean = false,
    syncStatusText: String = if (syncEnabled) "서버 동기화 사용 중" else "이 기기에 저장 중",
    syncInProgress: Boolean = false,
    deleteInProgress: Boolean = false,
    onRetrySync: (() -> Unit)? = null,
    onDeleteAllData: (() -> Unit)? = null,
) {
    var value by remember(currentNickname) { mutableStateOf(currentNickname) }
    var confirmDelete by remember { mutableStateOf(false) }
    if (!confirmDelete) AlertDialog(
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
                Text(syncStatusText, color = MoodprintColors.Primary)
                Text(
                    if (syncEnabled) "기록은 이 기기에 먼저 저장되고, 서버에는 사본이 저장될 수 있어요. 현재 재설치 후 복원은 지원하지 않아요. 전체 삭제를 선택하면 이 기기와 서버의 연결된 기록을 함께 삭제해요."
                    else "현재 기록은 이 기기에 저장돼요. 앱을 삭제하면 기기의 기록도 함께 삭제돼요.",
                    color = MoodprintColors.SecondaryText,
                )
                if (syncEnabled && onRetrySync != null) {
                    TextButton(onClick = onRetrySync, enabled = !syncInProgress && !deleteInProgress) {
                        Text(if (syncInProgress) "동기화 중…" else "지금 동기화 다시 시도")
                    }
                }
                if (onDeleteAllData != null) {
                    HorizontalDivider(color = MoodprintColors.Border)
                    TextButton(onClick = { confirmDelete = true }, enabled = !deleteInProgress && !syncInProgress) {
                        Text(if (deleteInProgress) "삭제 중…" else "모든 기록 삭제", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
    )
    if (confirmDelete) AlertDialog(
        onDismissRequest = { if (!deleteInProgress) confirmDelete = false },
        title = { Text("모든 기록을 삭제할까요?") },
        text = {
            Text(
                if (syncEnabled) "이 기기와 서버에 연결된 감정 기록, 행동 결과와 펫 진행도가 모두 삭제돼요. 이 작업은 되돌릴 수 없어요."
                else "이 기기의 감정 기록, 행동 결과와 펫 진행도가 모두 삭제돼요. 이 작업은 되돌릴 수 없어요."
            )
        },
        confirmButton = {
            TextButton(onClick = { onDeleteAllData?.invoke() }, enabled = !deleteInProgress) {
                Text(if (deleteInProgress) "삭제 중…" else "모두 삭제", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = { confirmDelete = false }, enabled = !deleteInProgress) { Text("취소") } },
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
