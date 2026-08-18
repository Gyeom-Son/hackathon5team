package com.moodprint.app.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moodprint.app.domain.AnimalKind
import com.moodprint.app.ui.components.MoodprintPet
import com.moodprint.app.ui.designsystem.MoodprintColors

/**
 * 16종 동물의 1·2·3단계 성장 모습을 한 번에 볼 수 있게 모아 보여주는 화면.
 * 도감 화면([MoodprintCollectionScreen])의 "전체 캐릭터 보기" 버튼으로만
 * 연결되어 있어서, 확인이 끝나면 이 파일과 CollectionScreen.kt의 해당 버튼 / 다이얼로그
 * 호출부만 지우면 깔끔하게 제거할 수 있다.
 */
@Composable
fun AnimalGalleryDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MoodprintColors.Background) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("캐릭터 전체 보기", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                }
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        "실제 데이터와 무관하게 항상 해금된 상태로 보여줘요. 확인이 끝나면 닫기를 눌러주세요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MoodprintColors.SecondaryText,
                    )
                    AnimalKind.entries.forEach { animal ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MoodprintColors.Surface,
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(animal.koreanName, style = MaterialTheme.typography.titleSmall)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    listOf(1, 2, 3).forEach { stage ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            MoodprintPet(
                                                size = 84,
                                                happy = true,
                                                colorName = animal.storageKey,
                                                name = animal.koreanName,
                                                unlocked = true,
                                                level = stage,
                                            )
                                            Text(
                                                "${stage}단계",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MoodprintColors.SecondaryText,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
