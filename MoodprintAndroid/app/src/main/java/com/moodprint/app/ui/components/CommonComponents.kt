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
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MoodprintColors.Surface, RoundedCornerShape(MoodprintRadius.Card))
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
) {
    Canvas(
        modifier
            .size(size.dp)
            .semantics {
                contentDescription = when {
                    !unlocked -> "아직 만나지 못한 마음 생물"
                    happy -> "기쁜 표정의 마음 동반자 $name"
                    else -> "편안한 표정의 마음 동반자 $name"
                }
            },
    ) {
        val width = this.size.width
        val height = this.size.height
        drawOval(
            Color(0x337060AD),
            androidx.compose.ui.geometry.Offset(width * .08f, height * .84f),
            androidx.compose.ui.geometry.Size(width * .84f, height * .14f),
        )
        val blob = Path().apply {
            moveTo(width * .5f, height * .04f)
            cubicTo(width * .82f, 0f, width, height * .2f, width * .98f, height * .5f)
            cubicTo(width, height * .8f, width * .8f, height * .86f, width * .5f, height * .84f)
            cubicTo(width * .2f, height * .86f, 0f, height * .8f, width * .02f, height * .5f)
            cubicTo(0f, height * .2f, width * .18f, 0f, width * .5f, height * .04f)
            close()
        }
        drawPath(blob, tint)
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
        if (happy) {
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
