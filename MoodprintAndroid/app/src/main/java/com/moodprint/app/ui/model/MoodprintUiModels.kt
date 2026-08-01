package com.moodprint.app.ui.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.moodprint.app.domain.RecoveryAction

enum class ActionChangeUi(val label: String) {
    HARDER("더 힘들어요"),
    SAME("비슷해요"),
    BETTER("조금 나아졌어요"),
    MUCH_BETTER("많이 나아졌어요"),
    SKIP("기록하지 않음"),
}

data class RecoveryActionUiModel(
    val id: String,
    val title: String,
    val instruction: String,
    val durationSeconds: Int,
    val icon: ImageVector,
    val categoryLabel: String,
    val detailPrompt: String? = null,
    val detailPlaceholder: String? = null,
)

fun RecoveryAction.toUiModel(icon: ImageVector) = RecoveryActionUiModel(
    id = id,
    title = title,
    instruction = instruction,
    durationSeconds = durationSeconds,
    icon = icon,
    categoryLabel = category.label,
    detailPrompt = detailPrompt,
    detailPlaceholder = detailPlaceholder,
)
