package com.moodprint.app.ui.reward

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodprint.app.ui.components.MoodprintButton
import com.moodprint.app.ui.components.MoodprintCard
import com.moodprint.app.ui.components.MoodprintEyebrow
import com.moodprint.app.ui.components.MoodprintPage
import com.moodprint.app.ui.components.MoodprintPet
import com.moodprint.app.ui.components.MoodprintStatusTag
import com.moodprint.app.ui.designsystem.MoodprintColors
import com.moodprint.app.ui.designsystem.petTintFor
import com.moodprint.app.ui.model.ActionChangeUi

@Composable
fun RewardScreen(
    experience: Int,
    fragments: Int,
    unlockedPetName: String?,
    onHome: () -> Unit,
    onCollection: () -> Unit,
    modifier: Modifier = Modifier,
    change: ActionChangeUi? = null,
    petName: String = "몽실이",
    petLevel: Int = 1,
    petColorName: String = "lavender",
    unlockedPetColorName: String? = null,
    didLevelUp: Boolean = false,
) = MoodprintPage(modifier) {
    Column(
        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MoodprintEyebrow("행동 완료")
        Spacer(Modifier.height(18.dp))
        MoodprintPet(142, happy = true, name = petName, level = petLevel, tint = petTintFor(petColorName))
        Spacer(Modifier.height(14.dp))
        MoodprintStatusTag("✨ 성장 경험치 +$experience", MoodprintColors.Warm)
        Spacer(Modifier.height(14.dp))
        Text(
            if (didLevelUp) "$petName 단계가 올랐어요" else "$petName 성장 경험치를 얻었어요",
            Modifier.fillMaxWidth(),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            when (change) {
                null, ActionChangeUi.SKIP -> "기록하지 않아도 괜찮아요. 행동을 완료한 것만으로 충분해요."
                ActionChangeUi.HARDER, ActionChangeUi.SAME -> "변화가 크지 않아도 괜찮아요. 나를 이해하는 중요한 기록이에요."
                ActionChangeUi.BETTER, ActionChangeUi.MUCH_BETTER -> "작은 행동을 완료한 오늘의 경험을 기억해둘게요."
            },
            Modifier.fillMaxWidth(),
            color = MoodprintColors.SecondaryText,
            textAlign = TextAlign.Center,
        )
        if (fragments > 0) {
            Spacer(Modifier.height(14.dp))
            MoodprintCard(color = MoodprintColors.SoftPurple) {
                if (unlockedPetName != null) {
                    MoodprintPet(
                        size = 76,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        happy = true,
                        name = unlockedPetName,
                        tint = petTintFor(unlockedPetColorName ?: "lavender"),
                    )
                }
                Text(
                    if (unlockedPetName != null) "$unlockedPetName 발견!" else "새로운 도감 조각 발견!",
                    Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    if (unlockedPetName != null) "새로운 마음 생물이 도감에 추가됐어요."
                    else "행동을 완료해 마음 생물 조각을 얻었어요.",
                    Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
    MoodprintButton("도감 확인하기", onCollection)
    OutlinedButton(
        onClick = onHome,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text("홈으로")
    }
}
