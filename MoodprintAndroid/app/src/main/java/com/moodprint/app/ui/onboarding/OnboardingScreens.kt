package com.moodprint.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodprint.app.ui.components.MoodprintButton
import com.moodprint.app.ui.components.MoodprintEyebrow
import com.moodprint.app.ui.components.MoodprintNavigationBackButton
import com.moodprint.app.ui.components.MoodprintPage
import com.moodprint.app.ui.components.MoodprintPet
import com.moodprint.app.ui.designsystem.MoodprintColors

@Composable
fun WelcomeScreen(onStart: () -> Unit, modifier: Modifier = Modifier) = MoodprintPage(modifier) {
    Column(
        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MoodprintEyebrow("MOODPRINT")
        Spacer(Modifier.height(24.dp))
        MoodprintPet(150, happy = true)
        Spacer(Modifier.height(24.dp))
        Text(
            "나에게 맞는\n작은 회복 행동을 발견해요",
            Modifier.fillMaxWidth(),
            fontSize = 30.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.Bold,
            color = MoodprintColors.Ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "감정과 행동의 변화를 기록하며\n나만의 마음 생물 도감을 채워보세요.",
            Modifier.fillMaxWidth(),
            color = MoodprintColors.SecondaryText,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
        )
    }
    MoodprintButton("시작하기", onStart)
}

@Composable
fun IntroScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) = MoodprintPage(modifier) {
    MoodprintNavigationBackButton(onBack)
    Column(
        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MoodprintPet(158)
        Spacer(Modifier.height(20.dp))
        Text(
            "안녕! 나는 몽실이야.",
            Modifier.fillMaxWidth(),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text("네 마음의 변화를 함께 기록할 동반자야.", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "작은 행동을 실험할수록\n새로운 마음 생물을 발견할 수 있어.",
            Modifier.fillMaxWidth(),
            color = MoodprintColors.SecondaryText,
            textAlign = TextAlign.Center,
        )
    }
    MoodprintButton("내 마음 기록하기", onContinue)
}

@Composable
fun ProfileScreen(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) = MoodprintPage(modifier) {
    MoodprintNavigationBackButton(onBack)
    Column(
        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.AccountCircle, contentDescription = null, Modifier.size(70.dp), tint = MoodprintColors.Primary)
        Spacer(Modifier.height(18.dp))
        Text("어떻게 불러드릴까요?", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(
            "계정을 만들지 않고 이 기기에서만 사용할 닉네임이에요.",
            Modifier.fillMaxWidth(),
            color = MoodprintColors.SecondaryText,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("닉네임") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(6.dp))
        Text("입력하지 않으면 ‘마음 여행자’로 시작해요.", color = MoodprintColors.SecondaryText, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
    }
    MoodprintButton("이 기기에서 시작하기", onComplete)
}
