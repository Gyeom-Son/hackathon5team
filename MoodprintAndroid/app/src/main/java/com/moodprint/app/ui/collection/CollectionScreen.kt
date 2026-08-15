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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.moodprint.app.ui.designsystem.petTintFor

@Composable
fun MoodprintCollectionScreen(
    pets: List<PetProgressEntity>,
    modifier: Modifier = Modifier,
) {
    val useSingleColumn = LocalDensity.current.fontScale >= 1.3f
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
                Text("발견한 친구들", style = MaterialTheme.typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Surface(color = MoodprintColors.SoftPurple, shape = RoundedCornerShape(MoodprintRadius.Pill)) {
                Text("${pets.count { it.isUnlocked }} / ${pets.size.coerceAtLeast(4)}", Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (useSingleColumn) 1 else 2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(MoodprintSpacing.Medium),
        ) {
            items(pets, key = { it.id }) { pet ->
                MoodprintPetCard(pet) { tint, unlocked ->
                    MoodprintPet(
                        size = 78,
                        happy = unlocked,
                        tint = tint,
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
}
