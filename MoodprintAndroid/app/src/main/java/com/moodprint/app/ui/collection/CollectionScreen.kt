package com.moodprint.app.ui.collection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import com.moodprint.app.data.local.PetProgressEntity
import com.moodprint.app.ui.designsystem.MoodprintColors
import com.moodprint.app.ui.designsystem.MoodprintRadius
import com.moodprint.app.ui.designsystem.MoodprintSpacing
import com.moodprint.app.ui.components.MoodprintPet
import com.moodprint.app.ui.debug.AnimalGalleryDialog

@Composable
fun MoodprintCollectionScreen(
    pets: List<PetProgressEntity>,
    modifier: Modifier = Modifier,
    onSetPrimaryPet: ((petId: String) -> Unit)? = null,
) {
    val useSingleColumn = LocalDensity.current.fontScale >= 1.3f
    var selectedPet by remember { mutableStateOf<PetProgressEntity?>(null) }
    // 16종 전체 성장 단계를 한 번에 볼 수 있다. 확인이 끝나면 이 상태, 아래
    // showAnimalGallery 관련 TextButton/다이얼로그 블록, 그리고 ui/debug/AnimalGalleryScreen.kt
    // 파일을 지우면 깔끔하게 제거할 수 있다.
    var showAnimalGallery by remember { mutableStateOf(false) }
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
                Text("마음 생물 도감", style = MaterialTheme.typography.labelSmall, color = MoodprintColors.Primary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("발견한 친구들", style = MaterialTheme.typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    // 진입점 (제거 가능, 아래 showAnimalGallery 선언부 주석 참고).
                    Surface(
                        onClick = { showAnimalGallery = true },
                        color = MoodprintColors.SoftPurple,
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            "전체 캐릭터 보기",
                            style = MaterialTheme.typography.labelSmall,
                            color = MoodprintColors.Primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
            }
            Surface(color = MoodprintColors.SoftPurple, shape = RoundedCornerShape(MoodprintRadius.Pill)) {
                Text("${pets.count { it.isUnlocked }} / ${pets.size.coerceAtLeast(16)}", Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (useSingleColumn) 1 else 2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(MoodprintSpacing.Medium),
        ) {
            items(pets, key = { it.id }) { pet ->
                MoodprintPetCard(
                    pet = pet,
                    onSelect = onSetPrimaryPet?.let { { selectedPet = pet } },
                ) { colorName, unlocked ->
                    MoodprintPet(
                        size = 78,
                        happy = unlocked,
                        colorName = colorName,
                        name = pet.name,
                        unlocked = unlocked,
                        level = pet.level,
                    )
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MoodprintColors.Surface,
            shape = RoundedCornerShape(MoodprintRadius.Card),
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Small)) {
                Text("천천히 발견해요", style = MaterialTheme.typography.labelSmall, color = MoodprintColors.Primary)
                Text("기록하지 않은 날에도 불이익은 없어요. 작은 행동을 완료할 때 새로운 친구를 만날 수 있어요.")
            }
        }
    }

    if (showAnimalGallery) {
        AnimalGalleryDialog(onDismiss = { showAnimalGallery = false })
    }

    val pet = selectedPet
    if (pet != null) {
        AlertDialog(
            onDismissRequest = { selectedPet = null },
            title = { Text(pet.name) },
            text = { Text("홈 화면에 표시되는 동반자를 ${pet.name}(으)로 바꿔요. 성장치는 각 친구별로 그대로 유지돼요.") },
            confirmButton = {
                TextButton(onClick = {
                    onSetPrimaryPet?.invoke(pet.id)
                    selectedPet = null
                }) { Text("${pet.name} 대표로 설정") }
            },
            dismissButton = { TextButton(onClick = { selectedPet = null }) { Text("취소") } },
        )
    }
}
