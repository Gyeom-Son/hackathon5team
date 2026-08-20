package com.moodprint.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodprint.app.R
import com.moodprint.app.domain.AnimalKind
import com.moodprint.app.domain.growthStageFor
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

/**
 * 16종 동물(고양이·강아지·토끼·곰·여우·판다·사자·호랑이·코알라·다람쥐·펭귄·부엉이·양·돼지·사슴·병아리)을
 * 성장 3단계(아기 → 성장 → 완성형)로 그려주는 펫 캐릭터입니다.
 *
 * 좌표는 `mongsili_animal_growth_stages_2.html` 참고자료의 `.stage`(90x96) / `.body`(80x74, 몸통 위쪽에
 * 14px 여백)를 그대로 비율로 옮긴 것입니다. 몸통 위쪽에 실제 빈 공간(headroom)을 남겨야 귀·꼬리 같은
 * 장식이 몸통 실루엣에 묻히지 않고 또렷하게 보입니다.
 */
@Composable
fun MoodprintPet(
    size: Int,
    modifier: Modifier = Modifier,
    happy: Boolean = false,
    colorName: String = AnimalKind.CAT.storageKey,
    name: String = AnimalKind.CAT.koreanName,
    unlocked: Boolean = true,
    level: Int = 1,
) {
    val animal = AnimalKind.fromStorageKey(colorName)
    val stage = growthStageFor(level)
    val description = when {
        !unlocked -> "아직 만나지 못한 마음 생물"
        happy -> "기쁜 표정의 마음 동반자 $name, 성장 ${level.coerceAtLeast(1)}단계"
        else -> "편안한 표정의 마음 동반자 $name, 성장 ${level.coerceAtLeast(1)}단계"
    }

    if (unlocked) {
        val growthScale = when (stage) { 1 -> .70f; 2 -> .90f; else -> 1f }
        Box(
            modifier = modifier
                .size(size.dp)
                .semantics { contentDescription = description },
            contentAlignment = Alignment.BottomCenter,
        ) {
            Image(
                painter = painterResource(animal.mascotDrawable(stage)),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(growthScale),
                contentScale = ContentScale.Fit,
            )
        }
        return
    }

    Canvas(
        modifier
            .size(size.dp)
            .alpha(if (unlocked) 1f else .38f)
            .semantics { contentDescription = description },
    ) {
        val width = this.size.width
        val height = this.size.height
        val bodyTint = if (unlocked) animal.bodyColor else Color(0xFFAAA5B2)
        val bodyScale = when (stage) { 1 -> .62f; 2 -> .84f; else -> 1f }
        val featureScale = when (stage) { 1 -> .78f; 2 -> .89f; else -> 1f }
        val faceScale = when (stage) { 1 -> 1.1f; 2 -> 1.04f; else -> 1f }
        // 시안의 낮고 넓은 찹쌀떡 체형은 바닥에 붙은 축을 기준으로 성장시킨다.
        val bottomPivot = Offset(width / 2f, height * .92f)
        // 참고 HTML의 transform-origin: 50% 12% (몸통 상단 쪽 장식 기준점).
        val featurePivot = Offset(width / 2f, height * .24f)

        drawOval(Color(0x12514955), Offset(width * .27f, height * .91f), Size(width * .46f, height * .035f))

        // 갈기·꼬리처럼 몸통 뒤에 깔리는 장식 — 몸통 실루엣 바깥으로 나오는 부분만 보인다.
        scale(bodyScale, pivot = bottomPivot) {
            scale(featureScale, pivot = featurePivot) {
                if (animal == AnimalKind.LION) drawLionMane(width, height, bodyTint.darker(.14f))
                if (animal in behindTailAnimals) {
                    drawTail(width, height, tailColor(animal, bodyTint), small = animal == AnimalKind.PIG)
                }
                if (animal == AnimalKind.SQUIRREL) drawSquirrelTail(width, height, bodyTint.darker(.1f))
            }
        }

        // 몸통 — 위쪽 약 15%는 비워 두어 귀가 들어갈 자리를 만든다.
        scale(bodyScale, pivot = bottomPivot) {
            drawPath(blobPath(width, height), bodyTint)
            drawPath(
                blobPath(width, height),
                mascotInk.copy(alpha = .78f),
                style = Stroke(width = width * .011f),
            )
        }

        if (!unlocked) return@Canvas

        // 귀·수염·줄무늬 등 몸통 앞 장식 + 얼굴.
        scale(bodyScale, pivot = bottomPivot) {
            scale(featureScale, pivot = featurePivot) {
                drawEars(animal, width, height, bodyTint.darker(.1f))
                when (animal) {
                    AnimalKind.CAT -> drawWhiskers(width, height)
                    AnimalKind.DOG -> drawDogMuzzle(width, height)
                    AnimalKind.TIGER -> {
                        drawStripes(width, height)
                        drawMuzzlePatch(width, height)
                    }
                    AnimalKind.FOX -> drawMuzzlePatch(width, height)
                    AnimalKind.BEAR -> drawBearMuzzle(width, height)
                    AnimalKind.PANDA -> drawEyePatches(width, height)
                    AnimalKind.OWL -> drawOwlFaceDisc(width, height)
                    AnimalKind.SHEEP -> {
                        drawSheepFacePatch(width, height)
                        drawWool(width, height)
                    }
                    AnimalKind.DEER -> {
                        drawAntlers(width, height)
                        drawDeerSpots(width, height)
                    }
                    AnimalKind.SQUIRREL -> drawCheekPuffs(width, height)
                    else -> Unit
                }
                if (animal == AnimalKind.PENGUIN) drawBelly(width, height)
                if (animal in wingedAnimals) drawWings(width, height, bodyTint.darker(.1f))
                if (animal == AnimalKind.CHICK) drawTuft(width, height, bodyTint.darker(.1f))
                if (animal == AnimalKind.PIG) drawSnout(width, height)
                if (animal in wingedAnimals) drawBeak(width, height)
            }
            scale(faceScale, pivot = Offset(width / 2f, height * .5f)) {
                drawFace(animal, width, height, happy)
                if (stage >= 2) drawBlush(width, height)
            }
            drawBeanPaws(width, height, bodyTint, animal)
        }
    }
}

private val mascotGrowthResources = mapOf(
    AnimalKind.CAT to intArrayOf(R.drawable.mascot_cat_stage1, R.drawable.mascot_cat_stage2, R.drawable.mascot_cat_stage3),
    AnimalKind.DOG to intArrayOf(R.drawable.mascot_dog_stage1, R.drawable.mascot_dog_stage2, R.drawable.mascot_dog_stage3),
    AnimalKind.RABBIT to intArrayOf(R.drawable.mascot_rabbit_stage1, R.drawable.mascot_rabbit_stage2, R.drawable.mascot_rabbit_stage3),
    AnimalKind.BEAR to intArrayOf(R.drawable.mascot_bear_stage1, R.drawable.mascot_bear_stage2, R.drawable.mascot_bear_stage3),
    AnimalKind.FOX to intArrayOf(R.drawable.mascot_fox_stage1, R.drawable.mascot_fox_stage2, R.drawable.mascot_fox_stage3),
    AnimalKind.PANDA to intArrayOf(R.drawable.mascot_panda_stage1, R.drawable.mascot_panda_stage2, R.drawable.mascot_panda_stage3),
    AnimalKind.LION to intArrayOf(R.drawable.mascot_lion_stage1, R.drawable.mascot_lion_stage2, R.drawable.mascot_lion_stage3),
    AnimalKind.TIGER to intArrayOf(R.drawable.mascot_tiger_stage1, R.drawable.mascot_tiger_stage2, R.drawable.mascot_tiger_stage3),
    AnimalKind.KOALA to intArrayOf(R.drawable.mascot_koala_stage1, R.drawable.mascot_koala_stage2, R.drawable.mascot_koala_stage3),
    AnimalKind.SQUIRREL to intArrayOf(R.drawable.mascot_squirrel_stage1, R.drawable.mascot_squirrel_stage2, R.drawable.mascot_squirrel_stage3),
    AnimalKind.PENGUIN to intArrayOf(R.drawable.mascot_penguin_stage1, R.drawable.mascot_penguin_stage2, R.drawable.mascot_penguin_stage3),
    AnimalKind.OWL to intArrayOf(R.drawable.mascot_owl_stage1, R.drawable.mascot_owl_stage2, R.drawable.mascot_owl_stage3),
    AnimalKind.SHEEP to intArrayOf(R.drawable.mascot_sheep_stage1, R.drawable.mascot_sheep_stage2, R.drawable.mascot_sheep_stage3),
    AnimalKind.PIG to intArrayOf(R.drawable.mascot_pig_stage1, R.drawable.mascot_pig_stage2, R.drawable.mascot_pig_stage3),
    AnimalKind.DEER to intArrayOf(R.drawable.mascot_deer_stage1, R.drawable.mascot_deer_stage2, R.drawable.mascot_deer_stage3),
    AnimalKind.CHICK to intArrayOf(R.drawable.mascot_chick_stage1, R.drawable.mascot_chick_stage2, R.drawable.mascot_chick_stage3),
)

private fun AnimalKind.mascotDrawable(stage: Int): Int =
    mascotGrowthResources.getValue(this)[growthStageFor(stage) - 1]

private val behindTailAnimals = listOf(AnimalKind.CAT, AnimalKind.DOG, AnimalKind.FOX, AnimalKind.LION, AnimalKind.PIG)
private val wingedAnimals = listOf(AnimalKind.PENGUIN, AnimalKind.OWL, AnimalKind.CHICK)
private val mascotInk = Color(0xFF514955)

private fun tailColor(animal: AnimalKind, bodyTint: Color): Color =
    if (animal == AnimalKind.FOX) Color.White.copy(alpha = .9f) else bodyTint.darker(.1f)

/**
 * 몽실몽실한 마스코트 실루엣. 가로(폭 .88)에 비해 세로가 짧아(.75) 살짝 눌린 타원처럼 보이던
 * 이전 버전을 세로로 늘려(.86) 원에 가깝게 만들었다 — 귀가 들어갈 위쪽 여백은 그대로 남긴다.
 */
private fun blobPath(width: Float, height: Float): Path = Path().apply {
    moveTo(width * .50f, height * .19f)
    cubicTo(width * .78f, height * .155f, width * .95f, height * .31f, width * .94f, height * .57f)
    cubicTo(width * .95f, height * .81f, width * .78f, height * .925f, width * .50f, height * .91f)
    cubicTo(width * .22f, height * .925f, width * .05f, height * .81f, width * .06f, height * .57f)
    cubicTo(width * .05f, height * .31f, width * .22f, height * .155f, width * .50f, height * .19f)
    close()
}

private fun DrawScope.drawEars(animal: AnimalKind, width: Float, height: Float, color: Color) {
    when (animal) {
        AnimalKind.CAT -> {
            drawTriangle(width * .27f, height * .115f, width * .24f, height * .23f, -14f, color)
            drawTriangle(width * .73f, height * .115f, width * .24f, height * .23f, 14f, color)
        }
        AnimalKind.FOX -> {
            // 고양이보다 더 길고 뾰족한 귀 + 짙은 귀 끝으로 여우임을 구분한다.
            // 귀 몸통과 귀 끝 무늬가 서로 다른 축을 기준으로 따로 회전되어 있어서 회전 각도만큼
            // 서로 어긋나 보였다 — 두 도형을 귀 중심 축 하나로 함께 회전시켜 정확히 맞춘다.
            val tipColor = Color(0xFF2C2C34).copy(alpha = .55f)
            listOf(-1f, 1f).forEach { sign ->
                val cx = width * .5f + sign * width * .24f
                val cy = height * .07f
                rotate(sign * 12f, pivot = Offset(cx, cy)) {
                    drawTrianglePath(cx, cy, width * .22f, height * .28f, color)
                    drawTrianglePath(cx, cy - height * .10f, width * .11f, height * .11f, tipColor)
                }
            }
        }
        AnimalKind.DOG -> {
            // 토끼처럼 솟아 보이지 않도록 짧고 넓은 타원을 머리 양옆에서 바깥으로 눕힌다.
            listOf(-1f, 1f).forEach { sign ->
                val cx = width * .5f + sign * width * .30f
                val cy = height * .25f
                rotate(sign * 32f, pivot = Offset(cx, cy)) {
                    drawOval(color, topLeft = Offset(cx - width * .15f, cy - height * .12f), size = Size(width * .30f, height * .24f))
                    drawOval(
                        Color(0xFFFFD7E0).copy(alpha = .55f),
                        topLeft = Offset(cx - width * .08f, cy - height * .055f),
                        size = Size(width * .16f, height * .11f),
                    )
                }
            }
        }
        AnimalKind.RABBIT -> {
            // 귀가 몸통에서 붕 떠 보이지 않을 만큼만 겹치게 하되(.14는 너무 내려와 얼굴에 파묻혀 보였다),
            // 토끼 특유의 길게 솟은 귀 느낌은 유지한다.
            drawCapsule(width * .31f, height * -.02f, width * .16f, height * .46f, -8f, color)
            drawCapsule(width * .69f, height * -.02f, width * .16f, height * .46f, 8f, color)
        }
        AnimalKind.BEAR -> drawRoundEarPair(width, height * .09f, dotted = true, diameter = width * .29f, spreadX = .28f, color = color)
        AnimalKind.PANDA -> drawRoundEarPair(width, height * .09f, dotted = false, diameter = width * .29f, spreadX = .28f, color = Color(0xFF2C2C34))
        AnimalKind.LION -> drawRoundEarPair(width, height * .125f, dotted = true, diameter = width * .22f, spreadX = .31f, color = color)
        AnimalKind.TIGER -> drawRoundEarPair(width, height * .115f, dotted = true, diameter = width * .20f, spreadX = .32f, color = color)
        AnimalKind.KOALA -> drawRoundEarPair(width, height * .156f, dotted = true, diameter = width * .33f, dotScale = .5f, spreadX = .40f, color = color)
        AnimalKind.SQUIRREL -> drawRoundEarPair(width, height * .135f, dotted = false, diameter = width * .156f, spreadX = .30f, color = color)
        AnimalKind.OWL -> {
            drawTriangle(width * .356f, height * .104f, width * .11f, height * .10f, -10f, color)
            drawTriangle(width * .644f, height * .104f, width * .11f, height * .10f, 10f, color)
        }
        AnimalKind.SHEEP -> {
            drawTriangle(width * .267f, height * .234f, width * .156f, height * .135f, -10f, Color(0xFFC9A27A))
            drawTriangle(width * .733f, height * .234f, width * .156f, height * .135f, 10f, Color(0xFFC9A27A))
        }
        AnimalKind.PIG -> {
            drawTriangle(width * .267f, height * .109f, width * .156f, height * .135f, -10f, color)
            drawTriangle(width * .733f, height * .109f, width * .156f, height * .135f, 10f, color)
        }
        AnimalKind.DEER -> {
            drawTriangle(width * .267f, height * .172f, width * .156f, height * .135f, -10f, color)
            drawTriangle(width * .733f, height * .172f, width * .156f, height * .135f, 10f, color)
        }
        AnimalKind.PENGUIN, AnimalKind.CHICK -> Unit
    }
}

private fun DrawScope.drawTriangle(cx: Float, cy: Float, w: Float, h: Float, degrees: Float, color: Color) {
    rotate(degrees, pivot = Offset(cx, cy)) {
        drawTrianglePath(cx, cy, w, h, color)
    }
}

private fun DrawScope.drawTrianglePath(cx: Float, cy: Float, w: Float, h: Float, color: Color) {
    val path = Path().apply {
        moveTo(cx, cy - h / 2)
        lineTo(cx + w / 2, cy + h / 2)
        lineTo(cx - w / 2, cy + h / 2)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawCapsule(cx: Float, cy: Float, w: Float, h: Float, degrees: Float, color: Color) {
    rotate(degrees, pivot = Offset(cx, cy)) {
        drawRoundRect(
            color,
            topLeft = Offset(cx - w / 2, cy - h / 2),
            size = Size(w, h),
            cornerRadius = CornerRadius(w / 2, w / 2),
        )
    }
}

private fun DrawScope.drawRoundEarPair(
    width: Float,
    earY: Float,
    dotted: Boolean,
    diameter: Float,
    color: Color,
    spreadX: Float = .26f,
    dotScale: Float = .42f,
) {
    val leftX = width * .5f - width * spreadX
    val rightX = width * .5f + width * spreadX
    listOf(leftX, rightX).forEach { cx ->
        drawCircle(color, radius = diameter / 2, center = Offset(cx, earY))
        if (dotted) {
            drawCircle(Color(0xFFFFD7E0), radius = diameter * dotScale / 2, center = Offset(cx, earY))
        }
    }
}

private fun DrawScope.drawTail(width: Float, height: Float, color: Color, small: Boolean) {
    // 몸통 뒤에 깔리는 장식이라 참고 HTML의 right:-14px처럼 몸통 실루엣 바깥으로 나와야 보인다.
    val d = if (small) width * .13f else width * .24f
    val cx = if (small) width * .98f else width * .93f
    val cy = if (small) height * .66f else height * .74f
    rotate(30f, pivot = Offset(cx, cy)) {
        drawRoundRect(
            color,
            topLeft = Offset(cx - d / 2, cy - d / 2),
            size = Size(d, d),
            cornerRadius = CornerRadius(d * .5f, d * .5f),
        )
    }
}

private fun DrawScope.drawSquirrelTail(width: Float, height: Float, color: Color) {
    // 참고 HTML: width30 height34 right:-20 bottom:14 rotate(10deg).
    rotate(10f, pivot = Offset(width * .90f, height * .59f)) {
        drawRoundRect(
            color,
            topLeft = Offset(width * .75f, height * .40f),
            size = Size(width * .30f, height * .38f),
            cornerRadius = CornerRadius(width * .14f, width * .14f),
        )
    }
}

private fun DrawScope.drawLionMane(width: Float, height: Float, color: Color) {
    // 뾰족한 톱니처럼 보이던 사각형 광선 대신, 겹치는 동그란 뭉치들로 몽실몽실한 갈기를 만든다.
    // 몸통(반경 약 .44)보다 살짝 크게 원을 두르고, 각 뭉치가 서로 겹치도록 지름 > 원 사이 간격으로 배치한다.
    val cx = width * .5f
    val cy = height * .53f
    val ringRadius = width * .40f
    val puffRadius = width * .155f
    val puffCount = 10
    for (i in 0 until puffCount) {
        val angle = Math.toRadians((i * (360.0 / puffCount)))
        val px = cx + (ringRadius * kotlin.math.cos(angle)).toFloat()
        val py = cy + (ringRadius * kotlin.math.sin(angle)).toFloat()
        drawCircle(color, radius = puffRadius, center = Offset(px, py))
    }
}

private fun DrawScope.drawWhiskers(width: Float, height: Float) {
    // 연보라 선, 그다음 흰색 선 모두 고양이 몸통(밝은 살구색)과 대비가 약해 거의 안 보였다.
    // 진한 잉크색으로 바꿔 배경색과 무관하게 뚜렷이 보이게 한다.
    val color = Color(0xFF3A3A44).copy(alpha = .9f)
    listOf(-1f, 1f).forEach { sign ->
        val baseX = width * .5f + sign * width * .44f
        val baseY = height * .58f
        drawLine(color, Offset(baseX, baseY - height * .06f), Offset(baseX + sign * width * .20f, baseY - height * .09f), strokeWidth = 2.2f)
        drawLine(color, Offset(baseX, baseY), Offset(baseX + sign * width * .21f, baseY), strokeWidth = 2.2f)
        drawLine(color, Offset(baseX, baseY + height * .06f), Offset(baseX + sign * width * .20f, baseY + height * .09f), strokeWidth = 2.2f)
    }
}

/** 여우·호랑이 특유의 밝은 턱·볼 무늬. 눈·코보다 먼저 그려서 그 위에 이목구비가 얹히게 한다. */
private fun DrawScope.drawMuzzlePatch(width: Float, height: Float) {
    drawOval(
        Color(0xFFFFF8EF).copy(alpha = .92f),
        topLeft = Offset(width * .5f - width * .17f, height * .46f),
        size = Size(width * .34f, height * .26f),
    )
}

/** 곰 특유의 밝은 주둥이: 눈 아래~입 주변을 밝은 색 타원으로 감싸 몸통과 구분되는 얼굴 부위를 만든다. */
private fun DrawScope.drawBearMuzzle(width: Float, height: Float) {
    drawOval(
        Color(0xFFF3E4C8).copy(alpha = .85f),
        topLeft = Offset(width * .5f - width * .14f, height * .49f),
        size = Size(width * .28f, height * .22f),
    )
}

/** 강아지는 얼굴 중앙의 크림색 주둥이와 처진 귀만으로 알아볼 수 있게 단순화한다. */
private fun DrawScope.drawDogMuzzle(width: Float, height: Float) {
    drawOval(
        Color(0xFFF3E6CE),
        topLeft = Offset(width * .5f - width * .15f, height * .48f),
        size = Size(width * .30f, height * .23f),
    )
}

/** 양의 얼굴은 털과 분리된 따뜻한 베이지색 한 덩어리로 표현한다. */
private fun DrawScope.drawSheepFacePatch(width: Float, height: Float) {
    drawOval(
        Color(0xFFC7A27B),
        topLeft = Offset(width * .5f - width * .22f, height * .30f),
        size = Size(width * .44f, height * .43f),
    )
}

private fun DrawScope.drawStripes(width: Float, height: Float) {
    // 이전 버전은 무늬 하나하나의 세로 길이(.16)가 줄 사이 간격(.04~.07)보다 훨씬 커서
    // 서로 겹쳐 뭉쳐 보였다. 길이를 짧게 줄이고 줄 사이 간격을 넉넉히 벌려 겹치지 않게 한다.
    val color = Color(0xFF2C2C34).copy(alpha = .88f)
    // (xFraction, y, angle) — 정수리 세로 무늬 1개 + 이마 대각선 2쌍.
    val stripeSpecs = listOf(
        Triple(.5f, height * .22f, 0.0),
        Triple(.36f, height * .27f, 14.0),
        Triple(.64f, height * .27f, -14.0),
        Triple(.30f, height * .34f, 24.0),
        Triple(.70f, height * .34f, -24.0),
    )
    stripeSpecs.forEach { (xFraction, y, angle) ->
        val cx = width * xFraction
        rotate(angle.toFloat(), pivot = Offset(cx, y)) {
            drawRoundRect(
                color,
                topLeft = Offset(cx - width * .0175f, y - height * .045f),
                size = Size(width * .035f, height * .09f),
                cornerRadius = CornerRadius(width * .0175f, width * .0175f),
            )
        }
    }
}

private fun DrawScope.drawEyePatches(width: Float, height: Float) {
    val color = Color(0xFF2C2C34).copy(alpha = .85f)
    listOf(-1f, 1f).forEach { sign ->
        rotate(sign * 15f, pivot = Offset(width * .5f + sign * width * .19f, height * .40f)) {
            drawOval(
                color,
                topLeft = Offset(width * .5f + sign * width * .19f - width * .1f, height * .40f - height * .08f),
                size = Size(width * .2f, height * .16f),
            )
        }
    }
}

/** 부엉이는 얼굴 원판 하나만 더해 깃털 장식 없이도 실루엣이 읽히게 한다. */
private fun DrawScope.drawOwlFaceDisc(width: Float, height: Float) {
    drawOval(
        Color(0xFFF5EBDD),
        topLeft = Offset(width * .5f - width * .25f, height * .31f),
        size = Size(width * .50f, height * .31f),
    )
}

/** 사슴의 이마 반점은 시안처럼 세 개만 사용해 복잡도를 억제한다. */
private fun DrawScope.drawDeerSpots(width: Float, height: Float) {
    val color = Color(0xFFF3E4C8).copy(alpha = .9f)
    drawCircle(color, radius = width * .025f, center = Offset(width * .5f, height * .27f))
    drawCircle(color, radius = width * .02f, center = Offset(width * .45f, height * .31f))
    drawCircle(color, radius = width * .02f, center = Offset(width * .55f, height * .31f))
}

private fun DrawScope.drawWool(width: Float, height: Float) {
    val color = Color(0xFFF7F4EC)
    val dxs = floatArrayOf(-.235f, -.078f, .078f, .235f)
    val dys = floatArrayOf(.135f, .167f, .167f, .135f)
    for (i in 0..3) {
        drawCircle(color, radius = width * .145f, center = Offset(width * .5f + dxs[i] * width, dys[i] * height))
    }
}

private fun DrawScope.drawAntlers(width: Float, height: Float) {
    val color = Color(0xFFA9835A)
    listOf(-1f, 1f).forEach { sign ->
        val baseX = width * .5f + sign * width * .20f
        val baseY = height * .14f
        val tipY = height * -.13f
        drawLine(color, Offset(baseX, baseY), Offset(baseX, tipY), strokeWidth = width * .022f)
        drawLine(
            color,
            Offset(baseX, baseY - (baseY - tipY) * .55f),
            Offset(baseX + sign * width * .08f, baseY - (baseY - tipY) * .8f),
            strokeWidth = width * .02f,
        )
    }
}

private fun DrawScope.drawCheekPuffs(width: Float, height: Float) {
    val color = Color.White.copy(alpha = .4f)
    listOf(-1f, 1f).forEach { sign ->
        drawOval(
            color,
            topLeft = Offset(width * .5f + sign * width * .36f - width * .085f, height * .58f - height * .07f),
            size = Size(width * .17f, height * .14f),
        )
    }
}

private fun DrawScope.drawBelly(width: Float, height: Float) {
    drawOval(
        Color.White.copy(alpha = .95f),
        topLeft = Offset(width * .5f - width * .26f, height * .58f),
        size = Size(width * .52f, height * .32f),
    )
}

private fun DrawScope.drawWings(width: Float, height: Float, color: Color) {
    val bottomY = height * .78f
    listOf(-1f, 1f).forEach { sign ->
        rotate(sign * 22f, pivot = Offset(width * .5f + sign * width * .47f, bottomY)) {
            drawOval(
                color,
                topLeft = Offset(width * .5f + sign * width * .47f - width * .08f, bottomY - height * .18f),
                size = Size(width * .16f, height * .28f),
            )
        }
    }
}

private fun DrawScope.drawTuft(width: Float, height: Float, color: Color) {
    rotate(-8f, pivot = Offset(width * .5f, height * .07f)) {
        drawRoundRect(
            color,
            topLeft = Offset(width * .45f, -height * .015f),
            size = Size(width * .1f, height * .16f),
            cornerRadius = CornerRadius(width * .05f, width * .05f),
        )
    }
}

private fun DrawScope.drawSnout(width: Float, height: Float) {
    drawOval(
        Color(0xFFEF9FAE),
        topLeft = Offset(width * .5f - width * .09f, height * .53f - height * .06f),
        size = Size(width * .18f, height * .12f),
    )
    listOf(-1f, 1f).forEach { sign ->
        drawOval(
            Color(0xFFC96B83),
            topLeft = Offset(width * .5f + sign * width * .035f - width * .015f, height * .53f - height * .02f),
            size = Size(width * .03f, height * .04f),
        )
    }
}

private fun DrawScope.drawBeak(width: Float, height: Float) {
    val cx = width * .5f
    val cy = height * .59f
    val path = Path().apply {
        moveTo(cx, cy + height * .035f)
        lineTo(cx + width * .04f, cy - height * .035f)
        lineTo(cx - width * .04f, cy - height * .035f)
        close()
    }
    drawPath(path, Color(0xFFF0A94E))
}

private fun DrawScope.drawFace(animal: AnimalKind, width: Float, height: Float, happy: Boolean) {
    val eyeY = height * .44f
    val eyeSpacing = when (animal) {
        AnimalKind.OWL -> .13f
        AnimalKind.BEAR, AnimalKind.PANDA, AnimalKind.LION -> .09f
        else -> .105f
    }
    drawMascotEyes(width, height, eyeY, eyeSpacing)

    val noseY = eyeY + height * .1f
    // 돼지는 별도 주둥이(snout)를 그리므로 기본 코는 생략하지만, 부리를 그리는 펭귄/부엉이/
    // 병아리와 달리 입은 그대로 그려야 한다 — 이전에는 여기서 통째로 return 해서 돼지가
    // 입이 없는 얼굴로 나왔다.
    val skipMouth = animal == AnimalKind.PENGUIN || animal == AnimalKind.OWL || animal == AnimalKind.CHICK
    when (animal) {
        AnimalKind.PENGUIN, AnimalKind.OWL, AnimalKind.CHICK, AnimalKind.PIG -> Unit
        AnimalKind.KOALA -> drawOval(Color(0xFF6B6B76), topLeft = Offset(width * .5f - width * .07f, noseY - height * .05f), size = Size(width * .14f, height * .1f))
        else -> {
            val noseColor = if (animal == AnimalKind.RABBIT || animal == AnimalKind.FOX) Color(0xFFEF8EA3) else Color(0xFFC96B6B)
            drawOval(noseColor, topLeft = Offset(width * .5f - width * .035f, noseY - height * .028f), size = Size(width * .07f, height * .056f))
        }
    }
    if (skipMouth) return

    val mouthY = noseY + height * .07f
    drawMascotMouth(width, height, mouthY, happy)
}

/** 모든 종이 같은 가족으로 보이게 만드는 공통 '담백 초롱눈'. 흰 점은 눈마다 정확히 하나다. */
private fun DrawScope.drawMascotEyes(
    width: Float,
    height: Float,
    eyeY: Float,
    spacing: Float,
) {
    listOf(-1f, 1f).forEach { sign ->
        val cx = width * .5f + sign * width * spacing
        drawOval(
            mascotInk,
            topLeft = Offset(cx - width * .032f, eyeY - height * .043f),
            size = Size(width * .064f, height * .086f),
        )
        drawCircle(
            Color.White,
            radius = width * .0115f,
            center = Offset(cx - width * .010f, eyeY - height * .018f),
        )
    }
}

/** 과장된 웃음 대신 작은 W 입을 사용하고, happy 상태에서는 폭만 살짝 넓힌다. */
private fun DrawScope.drawMascotMouth(width: Float, height: Float, mouthY: Float, happy: Boolean) {
    val halfWidth = width * if (happy) .065f else .052f
    val mouthHeight = height * if (happy) .075f else .062f
    val stroke = Stroke(width = width * .011f)
    drawArc(
        mascotInk,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(width * .5f - halfWidth, mouthY - mouthHeight * .5f),
        size = Size(halfWidth, mouthHeight),
        style = stroke,
    )
    drawArc(
        mascotInk,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(width * .5f, mouthY - mouthHeight * .5f),
        size = Size(halfWidth, mouthHeight),
        style = stroke,
    )
}

private fun DrawScope.drawBlush(width: Float, height: Float) {
    listOf(-1f, 1f).forEach { sign ->
        drawOval(
            Color(0xFFC97F86).copy(alpha = .28f),
            topLeft = Offset(width * .5f + sign * width * .22f - width * .045f, height * .535f),
            size = Size(width * .09f, height * .055f),
        )
    }
}

/** 시안의 핵심인 안쪽으로 모인 작은 콩알 앞발. 모든 동물에 같은 위치와 선 굵기를 쓴다. */
private fun DrawScope.drawBeanPaws(width: Float, height: Float, bodyTint: Color, animal: AnimalKind) {
    val pawColor = when (animal) {
        AnimalKind.PANDA, AnimalKind.PENGUIN -> Color(0xFF514955)
        AnimalKind.SHEEP -> Color(0xFFC7A27B)
        else -> bodyTint.darker(.055f)
    }
    listOf(-1f, 1f).forEach { sign ->
        val cx = width * .5f + sign * width * .16f
        val topLeft = Offset(cx - width * .072f, height * .79f)
        val pawSize = Size(width * .144f, height * .115f)
        drawOval(pawColor, topLeft = topLeft, size = pawSize)
        drawOval(
            mascotInk.copy(alpha = .72f),
            topLeft = topLeft,
            size = pawSize,
            style = Stroke(width = width * .009f),
        )
    }
}

private fun Color.darker(amount: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[2] = (hsv[2] - amount).coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv))
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
