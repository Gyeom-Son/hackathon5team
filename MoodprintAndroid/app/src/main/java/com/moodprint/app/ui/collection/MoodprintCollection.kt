package com.moodprint.app.ui.collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moodprint.app.data.local.PetProgressEntity
import com.moodprint.app.ui.designsystem.MoodprintColors
import com.moodprint.app.ui.designsystem.MoodprintRadius
import com.moodprint.app.ui.designsystem.MoodprintSpacing
import com.moodprint.app.ui.components.MoodprintPet

/**
 * 목록 순서가 아니라 colorName(동물 종류)을 사용하므로 Room 정렬이 바뀌어도 캐릭터가 유지된다.
 * 해금된 카드([pet.isUnlocked] && ![pet.isPrimary])는 [onSelect]가 주어지면 탭해서
 * 홈 화면 대표 동반자로 바꿀 수 있다.
 */
@Composable
fun MoodprintPetCard(
    pet: PetProgressEntity,
    modifier: Modifier = Modifier,
    onSelect: (() -> Unit)? = null,
    petVisual: (@Composable (colorName: String, unlocked: Boolean) -> Unit)? = null,
) {
    val selectable = pet.isUnlocked && !pet.isPrimary && onSelect != null
    val description = if (pet.isUnlocked) {
        "${pet.name}, 성장 ${pet.level.coerceAtLeast(1)}단계${if (pet.isPrimary) ", 현재 동반자" else if (selectable) ", 탭하면 대표로 설정" else ""}"
    } else {
        "아직 만나지 못한 친구, 조각 ${pet.fragments}/${pet.requiredFragments}, 다음 만남까지 ${(pet.requiredFragments - pet.fragments).coerceAtLeast(0)}조각"
    }
    var cardModifier = modifier
        .fillMaxWidth()
        .clearAndSetSemantics {
            contentDescription = description
            if (selectable) onClick(label = "대표로 설정") { onSelect?.invoke(); true }
        }
    if (selectable) {
        cardModifier = cardModifier.clickable { onSelect?.invoke() }
    }
    Surface(
        modifier = cardModifier,
        color = if (pet.isUnlocked) MoodprintColors.SoftPurple.copy(alpha = 0.48f) else MoodprintColors.Surface,
        shape = RoundedCornerShape(MoodprintRadius.Card),
        border = if (pet.isPrimary) BorderStroke(2.dp, MoodprintColors.Primary) else null,
    ) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Small),
        ) {
            if (petVisual != null) petVisual(pet.colorName, pet.isUnlocked)
            else MoodprintPet(
                size = 82,
                colorName = pet.colorName,
                name = pet.name,
                unlocked = pet.isUnlocked,
                happy = pet.isUnlocked,
                level = pet.level,
            )
            Text(
                if (pet.isUnlocked) pet.name else "아직 만나지 못한 친구",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            if (!pet.isUnlocked) {
                LinearProgressIndicator(
                    progress = { pet.fragments.toFloat() / pet.requiredFragments.coerceAtLeast(1) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    color = MoodprintColors.Primary,
                    trackColor = MoodprintColors.Border,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!pet.isUnlocked) Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(15.dp), tint = MoodprintColors.SecondaryText)
                Text(
                    if (pet.isUnlocked) {
                        if (pet.isPrimary) "지금 대표 · ${pet.level.coerceAtLeast(1)}단계" else "성장 ${pet.level.coerceAtLeast(1)}단계"
                    } else "다음 만남까지 ${pet.requiredFragments - pet.fragments}조각",
                    color = if (pet.isPrimary) MoodprintColors.Primary else MoodprintColors.SecondaryText,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
