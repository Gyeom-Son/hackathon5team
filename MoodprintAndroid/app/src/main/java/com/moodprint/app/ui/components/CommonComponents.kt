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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodprint.app.domain.AnimalKind
import com.moodprint.app.domain.growthStageFor
import com.moodprint.app.ui.designsystem.MoodprintColors
import com.moodprint.app.ui.designsystem.MoodprintPrimaryButton
import com.moodprint.app.ui.designsystem.MoodprintRadius
import com.moodprint.app.ui.designsystem.MoodprintSpacing
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

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
        val bodyTint = if (unlocked) animal.bodyColor else Color(0xFFAAA5B2)
        val bodyScale = when (stage) { 1 -> .62f; 2 -> .84f; else -> 1f }
        val featureScale = when (stage) { 1 -> .78f; 2 -> .89f; else -> 1f }
        val faceScale = when (stage) { 1 -> 1.1f; 2 -> 1.04f; else -> 1f }
        // 몸통 하단 중앙 — 몸통을 더 둥글게 늘리면서 바닥선이 .96 지점으로 내려간 것에 맞췄다.
        val bottomPivot = Offset(width / 2f, height * .96f)
        // 참고 HTML의 transform-origin: 50% 12% (몸통 상단 쪽 장식 기준점).
        val featurePivot = Offset(width / 2f, height * .24f)

        drawOval(
            Color(0x1F5A3C64),
            Offset(width * .21f, height * .96f),
            Size(width * .58f, height * .05f),
        )

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
        }

        if (!unlocked) return@Canvas

        // 귀·수염·줄무늬 등 몸통 앞 장식 + 얼굴.
        scale(bodyScale, pivot = bottomPivot) {
            scale(featureScale, pivot = featurePivot) {
                // 반짝임을 귀보다 먼저(아래에) 그려서, 귀와 겹치는 자리에서는 귀 뒤로 자연스럽게
                // 가려지게 한다 (이전에는 맨 위에 그려서 귀를 가로지르는 것처럼 보였다).
                if (stage == 3) drawSparkle(width, height)
                drawEars(animal, width, height, bodyTint.darker(.1f))
                when (animal) {
                    AnimalKind.CAT -> drawWhiskers(width, height)
                    AnimalKind.TIGER -> {
                        drawStripes(width, height)
                        drawMuzzlePatch(width, height)
                    }
                    AnimalKind.FOX -> drawMuzzlePatch(width, height)
                    AnimalKind.BEAR -> drawBearMuzzle(width, height)
                    AnimalKind.PANDA -> drawEyePatches(width, height)
                    AnimalKind.SHEEP -> drawWool(width, height)
                    AnimalKind.DEER -> drawAntlers(width, height)
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
            }
        }
    }
}

private val behindTailAnimals = listOf(AnimalKind.CAT, AnimalKind.DOG, AnimalKind.FOX, AnimalKind.LION, AnimalKind.PIG)
private val wingedAnimals = listOf(AnimalKind.PENGUIN, AnimalKind.OWL, AnimalKind.CHICK)

private fun tailColor(animal: AnimalKind, bodyTint: Color): Color =
    if (animal == AnimalKind.FOX) Color.White.copy(alpha = .9f) else bodyTint.darker(.1f)

/**
 * 몽실몽실한 마스코트 실루엣. 가로(폭 .88)에 비해 세로가 짧아(.75) 살짝 눌린 타원처럼 보이던
 * 이전 버전을 세로로 늘려(.86) 원에 가깝게 만들었다 — 귀가 들어갈 위쪽 여백은 그대로 남긴다.
 */
private fun blobPath(width: Float, height: Float): Path = Path().apply {
    moveTo(width * .50f, height * .132f)
    cubicTo(width * .79f, height * .100f, width * .96f, height * .293f, width * .94f, height * .562f)
    cubicTo(width * .96f, height * .842f, width * .78f, height * .960f, width * .50f, height * .939f)
    cubicTo(width * .23f, height * .960f, width * .04f, height * .842f, width * .06f, height * .562f)
    cubicTo(width * .04f, height * .293f, width * .21f, height * .100f, width * .50f, height * .132f)
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
            // 자연스러운 처진 귀: 곧은 알약 모양 대신 통통한 타원으로 정수리 옆에 붙여
            // 뺨 옆으로 늘어지게 하고, 안쪽에 옅은 분홍을 더해 귀처럼 보이게 한다.
            // 중심에서 .40만큼 벌어져 있으면 몸통 실루엣 밖으로 반쯤 튀어나가 붕 떠 보인다
            // (심지어 프레임 밖으로도 잘렸다) — 중심으로 더 당기고 위로 올려 머리에 붙게 한다.
            listOf(-1f, 1f).forEach { sign ->
                val cx = width * .5f + sign * width * .32f
                val cy = height * .24f
                rotate(sign * 14f, pivot = Offset(cx, cy)) {
                    drawOval(color, topLeft = Offset(cx - width * .13f, cy - height * .21f), size = Size(width * .26f, height * .42f))
                    drawOval(
                        Color(0xFFFFD7E0).copy(alpha = .55f),
                        topLeft = Offset(cx - width * .07f, cy - height * .01f),
                        size = Size(width * .14f, height * .22f),
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
    val ink = MoodprintColors.Ink
    val eyeY = height * .44f
    when (animal) {
        AnimalKind.OWL -> {
            listOf(-1f, 1f).forEach { sign ->
                val cx = width * .5f + sign * width * .13f
                drawCircle(Color.White, radius = width * .09f, center = Offset(cx, eyeY))
                drawCircle(ink, radius = width * .045f, center = Offset(cx, eyeY))
            }
        }
        AnimalKind.TIGER, AnimalKind.FOX -> {
            // 얇은 실선 하나였던 이전 버전은 감은 눈처럼 보였다. 아몬드 모양을 두껍게 키우고
            // 회전을 줄여서 뜬 눈처럼 보이게 하고, 작은 하이라이트로 생기를 더한다.
            listOf(-1f, 1f).forEach { sign ->
                val cx = width * .5f + sign * width * .11f
                rotate(sign * 10f, pivot = Offset(cx, eyeY)) {
                    drawOval(ink, topLeft = Offset(cx - width * .045f, eyeY - height * .045f), size = Size(width * .09f, height * .09f))
                }
                drawCircle(Color.White, radius = width * .012f, center = Offset(cx + sign * width * .015f, eyeY - height * .02f))
            }
        }
        AnimalKind.KOALA -> {
            listOf(-1f, 1f).forEach { sign ->
                drawRoundRect(
                    ink,
                    topLeft = Offset(width * .5f + sign * width * .1f - width * .045f, eyeY - height * .0125f),
                    size = Size(width * .09f, height * .025f),
                    cornerRadius = CornerRadius(height * .012f, height * .012f),
                )
            }
        }
        AnimalKind.DEER -> {
            listOf(-1f, 1f).forEach { sign ->
                drawOval(ink, topLeft = Offset(width * .5f + sign * width * .11f - width * .045f, eyeY - height * .05f), size = Size(width * .09f, height * .1f))
            }
        }
        AnimalKind.BEAR, AnimalKind.PANDA, AnimalKind.LION, AnimalKind.SHEEP, AnimalKind.CHICK -> {
            listOf(-1f, 1f).forEach { sign ->
                drawCircle(ink, radius = width * .045f, center = Offset(width * .5f + sign * width * .08f, eyeY))
            }
        }
        else -> {
            listOf(-1f, 1f).forEach { sign ->
                drawCircle(ink, radius = width * .035f, center = Offset(width * .5f + sign * width * .1f, eyeY))
            }
        }
    }

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
    if (happy) {
        drawArc(
            ink,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(width * .5f - width * .07f, mouthY - height * .045f),
            size = Size(width * .14f, height * .09f),
            style = Stroke(width = width * .014f),
        )
    } else {
        drawLine(ink, Offset(width * .5f - width * .05f, mouthY), Offset(width * .5f + width * .05f, mouthY), strokeWidth = width * .014f)
    }
}

private fun DrawScope.drawSparkle(width: Float, height: Float) {
    val cx = width * .84f
    val cy = height * .16f
    val outerR = width * .07f
    val innerR = outerR * .42f
    val path = Path()
    for (i in 0 until 8) {
        val angle = Math.PI / 4 * i
        val r = if (i % 2 == 0) outerR else innerR
        val x = cx + (r * cos(angle)).toFloat()
        val y = cy + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, Color(0xFFF0A94E))
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
