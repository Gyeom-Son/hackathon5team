package com.moodprint.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodprint.app.ui.designsystem.MoodprintColors
import com.moodprint.app.ui.designsystem.MoodprintPrimaryButton
import com.moodprint.app.ui.designsystem.MoodprintRadius
import com.moodprint.app.ui.designsystem.MoodprintSpacing

@Composable
fun MoodprintPage(
    modifier: Modifier = Modifier,
    includeStatusBars: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val safeModifier = if (includeStatusBars) modifier.statusBarsPadding() else modifier
    Column(
        modifier = safeModifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(MoodprintSpacing.XLarge),
        verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Large),
        content = content,
    )
}

@Composable
fun MoodprintCard(
    modifier: Modifier = Modifier,
    color: Color = MoodprintColors.Surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(MoodprintRadius.Card))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(MoodprintSpacing.Small),
        content = content,
    )
}

@Composable
fun MoodprintEyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MoodprintColors.Primary,
        letterSpacing = 1.2.sp,
    )
}

@Composable
fun MoodprintNavigationBackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = MoodprintColors.Primary,
            )
        }
    }
}

@Composable
fun MoodprintPet(
    size: Int,
    modifier: Modifier = Modifier,
    happy: Boolean = false,
    tint: Color = MoodprintColors.PetPurple,
    name: String = "몽실이",
    unlocked: Boolean = true,
    level: Int = 1,
) {
    Canvas(
        modifier
            .size(size.dp)
            .alpha(if (unlocked) 1f else .38f)
            .semantics {
                contentDescription = when {
                    !unlocked -> "아직 만나지 못한 마음 생물"
                    happy -> "기쁜 표정의 마음 동반자 $name, 성장 ${level.coerceAtLeast(1)}단계"
                    else -> "편안한 표정의 마음 동반자 $name, 성장 ${level.coerceAtLeast(1)}단계"
                }
            },
    ) {
        val width = this.size.width
        val height = this.size.height
        val bodyTint = if (unlocked) tint else Color(0xFFAAA5B2)
        val variant = when {
            name.contains("폴짝") -> 1
            name.contains("끄적") || name.contains("끈적") -> 2
            name.contains("반짝") -> 3
            else -> 0
        }
        drawOval(
            Color(0x337060AD),
            androidx.compose.ui.geometry.Offset(width * .08f, height * .84f),
            androidx.compose.ui.geometry.Size(width * .84f, height * .14f),
        )
        // 펫마다 안정적인 귀·꼬리 실루엣을 사용한다.
        when (variant) {
            1 -> {
                drawOval(bodyTint, androidx.compose.ui.geometry.Offset(width * .18f, 0f), androidx.compose.ui.geometry.Size(width * .20f, height * .36f))
                drawOval(bodyTint, androidx.compose.ui.geometry.Offset(width * .62f, 0f), androidx.compose.ui.geometry.Size(width * .20f, height * .36f))
            }
            2 -> {
                drawCircle(bodyTint, width * .15f, androidx.compose.ui.geometry.Offset(width * .22f, height * .22f))
                drawCircle(bodyTint, width * .15f, androidx.compose.ui.geometry.Offset(width * .78f, height * .22f))
            }
            3 -> {
                val star = Path().apply {
                    moveTo(width * .50f, 0f); lineTo(width * .57f, height * .17f)
                    lineTo(width * .75f, height * .18f); lineTo(width * .61f, height * .29f)
                    lineTo(width * .66f, height * .45f); lineTo(width * .50f, height * .35f)
                    lineTo(width * .34f, height * .45f); lineTo(width * .39f, height * .29f)
                    lineTo(width * .25f, height * .18f); lineTo(width * .43f, height * .17f); close()
                }
                drawPath(star, bodyTint)
            }
            else -> if (level >= 2) {
                drawCircle(bodyTint, width * .12f, androidx.compose.ui.geometry.Offset(width * .18f, height * .24f))
                drawCircle(bodyTint, width * .12f, androidx.compose.ui.geometry.Offset(width * .82f, height * .24f))
            }
        }
        val growthInset = if (level >= 3) .0f else .02f
        val blob = Path().apply {
            moveTo(width * .5f, height * (.04f + growthInset))
            cubicTo(width * .82f, 0f, width, height * .2f, width * .98f, height * .5f)
            cubicTo(width, height * .8f, width * .8f, height * .86f, width * .5f, height * .84f)
            cubicTo(width * .2f, height * .86f, 0f, height * .8f, width * .02f, height * .5f)
            cubicTo(0f, height * .2f, width * .18f, 0f, width * .5f, height * .04f)
            close()
        }
        drawPath(blob, bodyTint)
        if (variant == 2) {
            drawLine(bodyTint, androidx.compose.ui.geometry.Offset(width * .92f, height * .55f), androidx.compose.ui.geometry.Offset(width, height * .40f), width * .08f)
        }
        // 잠긴 펫은 수집의 설렘을 유지하도록 얼굴이 없는 실루엣으로 보여준다.
        if (!unlocked) return@Canvas

        // 성장 2단계부터 짧은 팔과 발, 볼터치가 생겨 실루엣과 표정이 풍부해진다.
        if (level >= 2) {
            drawLine(bodyTint, androidx.compose.ui.geometry.Offset(width * .08f, height * .57f), androidx.compose.ui.geometry.Offset(width * .01f, height * .66f), width * .045f)
            drawLine(bodyTint, androidx.compose.ui.geometry.Offset(width * .92f, height * .57f), androidx.compose.ui.geometry.Offset(width * .99f, height * .66f), width * .045f)
            drawOval(bodyTint.copy(alpha = .82f), androidx.compose.ui.geometry.Offset(width * .24f, height * .78f), androidx.compose.ui.geometry.Size(width * .20f, height * .10f))
            drawOval(bodyTint.copy(alpha = .82f), androidx.compose.ui.geometry.Offset(width * .56f, height * .78f), androidx.compose.ui.geometry.Size(width * .20f, height * .10f))
            drawCircle(MoodprintColors.PetCoral.copy(alpha = .38f), width * .055f, androidx.compose.ui.geometry.Offset(width * .25f, height * .56f))
            drawCircle(MoodprintColors.PetCoral.copy(alpha = .38f), width * .055f, androidx.compose.ui.geometry.Offset(width * .75f, height * .56f))
        }

        // 펫별 배 무늬로 작은 크기에서도 개성을 구분한다.
        when (variant) {
            1 -> drawOval(Color.White.copy(alpha = .20f), androidx.compose.ui.geometry.Offset(width * .36f, height * .60f), androidx.compose.ui.geometry.Size(width * .28f, height * .17f))
            2 -> {
                drawLine(Color.White.copy(alpha = .32f), androidx.compose.ui.geometry.Offset(width * .32f, height * .69f), androidx.compose.ui.geometry.Offset(width * .68f, height * .62f), width * .025f)
                drawLine(Color.White.copy(alpha = .22f), androidx.compose.ui.geometry.Offset(width * .38f, height * .75f), androidx.compose.ui.geometry.Offset(width * .63f, height * .70f), width * .018f)
            }
            3 -> drawCircle(Color.White.copy(alpha = .35f), width * .07f, androidx.compose.ui.geometry.Offset(width * .50f, height * .68f))
            else -> if (level >= 3) {
                val leaf = Path().apply {
                    moveTo(width * .50f, height * .09f)
                    quadraticTo(width * .58f, -height * .03f, width * .68f, height * .04f)
                    quadraticTo(width * .61f, height * .13f, width * .50f, height * .09f)
                    close()
                }
                drawPath(leaf, MoodprintColors.PetMint)
                drawLine(MoodprintColors.Primary, androidx.compose.ui.geometry.Offset(width * .5f, height * .1f), androidx.compose.ui.geometry.Offset(width * .5f, height * .19f), width * .018f)
            }
        }
        drawCircle(
            MoodprintColors.Ink,
            width * .033f,
            androidx.compose.ui.geometry.Offset(width * .37f, height * .43f),
        )
        drawCircle(
            MoodprintColors.Ink,
            width * .033f,
            androidx.compose.ui.geometry.Offset(width * .63f, height * .43f),
        )
        if (level >= 3) {
            drawCircle(Color.White.copy(alpha = .9f), width * .010f, androidx.compose.ui.geometry.Offset(width * .38f, height * .42f))
            drawCircle(Color.White.copy(alpha = .9f), width * .010f, androidx.compose.ui.geometry.Offset(width * .64f, height * .42f))
        }
        if (level >= 4) {
            // 높은 성장 단계에서는 작은 빛 조각이 생겨 외형 변화가 멈추지 않는다.
            drawCircle(MoodprintColors.Warm, width * .035f, androidx.compose.ui.geometry.Offset(width * .16f, height * .30f))
            drawCircle(MoodprintColors.Warm, width * .025f, androidx.compose.ui.geometry.Offset(width * .84f, height * .34f))
            drawCircle(Color.White.copy(alpha = .45f), width * .035f, androidx.compose.ui.geometry.Offset(width * .50f, height * .28f))
        }
        if (happy || variant == 1) {
            val mouth = Path().apply {
                moveTo(width * .43f, height * .57f)
                quadraticTo(width * .5f, height * .66f, width * .57f, height * .57f)
            }
            drawPath(mouth, MoodprintColors.Ink, style = Stroke(width * .018f))
        } else {
            drawLine(
                MoodprintColors.Ink,
                androidx.compose.ui.geometry.Offset(width * .45f, height * .59f),
                androidx.compose.ui.geometry.Offset(width * .55f, height * .59f),
                width * .018f,
            )
        }
    }
}

@Composable
fun MoodprintStatusTag(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier, color = color, shape = RoundedCornerShape(MoodprintRadius.Pill)) {
        Text(
            label,
            Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
fun MoodprintButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = MoodprintPrimaryButton(text, onClick, modifier, enabled)
