package com.moodprint.app.ui.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object MoodprintColors {
    val Primary = Color(0xFF7060AD)
    val PrimaryPressed = Color(0xFF5C4A94)
    val Background = Color(0xFFF8F3FC)
    val Surface = Color(0xFFFFFFFF)
    val SoftPurple = Color(0xFFEAE2F7)
    val Mint = Color(0xFFD6EFE8)
    val Coral = Color(0xFFF7DBD4)
    val Warm = Color(0xFFF7E8BA)
    val Ink = Color(0xFF2E293F)
    val SecondaryText = Color(0xFF6E697D)
    val Border = Color(0xFFD7CFDF)

    val scheme: ColorScheme = lightColorScheme(
        primary = Primary,
        onPrimary = Color.White,
        primaryContainer = SoftPurple,
        onPrimaryContainer = Ink,
        background = Background,
        onBackground = Ink,
        surface = Surface,
        onSurface = Ink,
        outline = Border,
    )
}

object MoodprintSpacing {
    val XSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val XLarge = 20.dp
    val XXLarge = 24.dp
}

object MoodprintRadius {
    val Control = 15.dp
    val Card = 20.dp
    val Pill = 999.dp
}

object MoodprintSize {
    val MinimumTouchTarget = 48.dp
    val PrimaryButtonHeight = 54.dp
}

val MoodprintTypography = Typography(
    displaySmall = Typography().displaySmall.copy(
        fontSize = 30.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Bold,
    ),
    headlineMedium = Typography().headlineMedium.copy(
        fontSize = 28.sp,
        lineHeight = 35.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleLarge = Typography().titleLarge.copy(
        fontSize = 22.sp,
        lineHeight = 29.sp,
        fontWeight = FontWeight.Bold,
    ),
    titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = Typography().bodyLarge.copy(lineHeight = 24.sp),
    bodyMedium = Typography().bodyMedium.copy(lineHeight = 21.sp),
    labelLarge = Typography().labelLarge.copy(fontWeight = FontWeight.Bold),
)

@Composable
fun MoodprintDesignTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MoodprintColors.scheme,
        typography = MoodprintTypography,
        content = content,
    )
}

@Composable
fun MoodprintPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = MoodprintSize.PrimaryButtonHeight),
        shape = RoundedCornerShape(MoodprintRadius.Control),
        colors = ButtonDefaults.buttonColors(
            containerColor = MoodprintColors.Primary,
            disabledContainerColor = MoodprintColors.Primary.copy(alpha = 0.42f),
        ),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** 색 외에도 체크 표시와 selected semantics로 선택 상태를 전달한다. */
@Composable
fun MoodprintChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(minHeight = MoodprintSize.MinimumTouchTarget)
            .semantics {
                this.selected = selected
                role = Role.Checkbox
            },
        shape = RoundedCornerShape(MoodprintRadius.Control),
        color = if (selected) MoodprintColors.Primary else MoodprintColors.Surface,
        contentColor = if (selected) Color.White else MoodprintColors.Ink,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MoodprintColors.Primary else MoodprintColors.Border,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let {
                Icon(it, contentDescription = null, modifier = Modifier.padding(end = 5.dp))
            }
            Text(label, style = MaterialTheme.typography.labelLarge)
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 5.dp),
                )
            }
        }
    }
}

@Immutable
data class MoodprintChoice(
    val id: String,
    val label: String,
)

@Composable
fun MoodprintChoiceGridItem(
    choice: MoodprintChoice,
    selected: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(modifier) {
        MoodprintChoiceChip(
            label = choice.label,
            selected = selected,
            onClick = { onSelect(choice.id) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
        )
    }
}
