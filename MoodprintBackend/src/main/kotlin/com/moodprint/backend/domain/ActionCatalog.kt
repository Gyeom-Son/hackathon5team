package com.moodprint.backend.domain

data class RecoveryAction(
    val id: String, val title: String, val instruction: String, val durationSeconds: Int,
    val category: ActionCategory, val symbolName: String, val emotions: Set<MoodEmotion>,
    val energies: Set<MoodEnergy>, val order: Int, val detailPrompt: String? = null,
    val detailPlaceholder: String? = null,
)

object ActionCatalog {
    val actions = listOf(
        action(1, "좋아하는 음악 한 곡 듣기", "다른 일을 잠시 멈추고 음악 한 곡을 끝까지 들어보세요.", 180, ActionCategory.SENSORY, "headphones", setOf(MoodEmotion.ANXIOUS, MoodEmotion.UPSET, MoodEmotion.LONELY, MoodEmotion.COMPLICATED, MoodEmotion.SAD), MoodEnergy.entries.toSet(), "어떤 음악을 들었나요?", "곡이나 아티스트를 적어보세요"),
        action(2, "창문 열고 천천히 숨 쉬기", "편한 자세에서 바깥 공기를 느끼며 천천히 숨을 쉬어보세요.", 120, ActionCategory.REST, "wind", setOf(MoodEmotion.ANXIOUS, MoodEmotion.ANGRY, MoodEmotion.COMPLICATED, MoodEmotion.FRUSTRATED), setOf(MoodEnergy.LOW, MoodEnergy.MEDIUM)),
        action(3, "가볍게 몸 풀기", "어깨와 목부터 무리하지 않는 범위에서 가볍게 움직여보세요.", 180, ActionCategory.MOVEMENT, "figure.cooldown", setOf(MoodEmotion.LETHARGIC, MoodEmotion.ANGRY, MoodEmotion.UPSET, MoodEmotion.TIRED, MoodEmotion.FRUSTRATED), setOf(MoodEnergy.MEDIUM, MoodEnergy.HIGH)),
        action(4, "지금 떠오르는 말 적기", "정리하려 애쓰지 말고 지금 떠오르는 말을 짧게 적어보세요.", 180, ActionCategory.EXPRESSION, "pencil.line", setOf(MoodEmotion.UPSET, MoodEmotion.ANGRY, MoodEmotion.LONELY, MoodEmotion.COMPLICATED, MoodEmotion.SAD, MoodEmotion.FRUSTRATED), MoodEnergy.entries.toSet()),
        action(5, "따뜻한 물 한 잔 마시기", "물을 천천히 마시며 온도와 감각에 잠시 집중해보세요.", 120, ActionCategory.SENSORY, "mug", setOf(MoodEmotion.LETHARGIC, MoodEmotion.UPSET, MoodEmotion.LONELY, MoodEmotion.TIRED, MoodEmotion.SAD), setOf(MoodEnergy.LOW, MoodEnergy.MEDIUM)),
        action(6, "좋아하는 향 맡아보기", "차나 비누처럼 편하게 느껴지는 향에 잠시 집중해보세요.", 60, ActionCategory.SENSORY, "nose", setOf(MoodEmotion.ANXIOUS, MoodEmotion.COMPLICATED, MoodEmotion.FRUSTRATED), MoodEnergy.entries.toSet(), "어떤 향이었나요?", "기억하고 싶은 향을 적어보세요"),
        action(7, "차가운 물로 손 씻기", "차가운 물의 온도와 손끝의 감각을 천천히 느껴보세요.", 60, ActionCategory.SENSORY, "drop.fill", setOf(MoodEmotion.ANGRY, MoodEmotion.COMPLICATED, MoodEmotion.FRUSTRATED), setOf(MoodEnergy.MEDIUM, MoodEnergy.HIGH)),
        action(8, "보라색 물건 3개 찾기", "주변을 천천히 살피며 보라색 물건 세 개를 찾아보세요.", 120, ActionCategory.SENSORY, "eye.fill", setOf(MoodEmotion.ANXIOUS, MoodEmotion.COMPLICATED, MoodEmotion.FRUSTRATED), MoodEnergy.entries.toSet(), "무엇을 찾았나요?", "찾은 물건을 적어보세요"),
        action(9, "눈을 감고 어깨 힘 풀기", "편하게 앉아 어깨에 들어간 힘을 천천히 풀어보세요.", 120, ActionCategory.REST, "moon.zzz.fill", setOf(MoodEmotion.ANXIOUS, MoodEmotion.UPSET, MoodEmotion.TIRED, MoodEmotion.SAD), setOf(MoodEnergy.LOW)),
        action(10, "휴대폰을 뒤집고 잠시 쉬기", "휴대폰을 내려놓고 화면 없는 시간을 잠시 가져보세요.", 180, ActionCategory.REST, "iphone.slash", setOf(MoodEmotion.COMPLICATED, MoodEmotion.LETHARGIC, MoodEmotion.TIRED), setOf(MoodEnergy.LOW, MoodEnergy.MEDIUM)),
        action(11, "오늘 할 일 하나만 고르기", "해야 할 일을 모두 떠올리기보다 지금 할 수 있는 한 가지만 골라보세요.", 120, ActionCategory.REFLECTION, "lightbulb.fill", setOf(MoodEmotion.ANXIOUS, MoodEmotion.LETHARGIC, MoodEmotion.COMPLICATED, MoodEmotion.FRUSTRATED), setOf(MoodEnergy.LOW, MoodEnergy.MEDIUM), "어떤 할 일을 골랐나요?", "지금 할 일 하나를 적어보세요"),
        action(12, "지금 필요한 것 한 단어로 적기", "지금 나에게 필요한 것을 한 단어로 남겨보세요.", 120, ActionCategory.EXPRESSION, "text.cursor", setOf(MoodEmotion.COMPLICATED, MoodEmotion.UPSET, MoodEmotion.SAD), MoodEnergy.entries.toSet(), "지금 필요한 것은 무엇인가요?", "한 단어로 적어보세요"),
        action(13, "아주 작은 일 하나 끝내기", "물 한 잔 채우기처럼 3분 안에 할 수 있는 작은 일 하나를 끝내보세요.", 180, ActionCategory.MOVEMENT, "checkmark.seal.fill", setOf(MoodEmotion.LETHARGIC, MoodEmotion.COMPLICATED, MoodEmotion.TIRED), setOf(MoodEnergy.LOW, MoodEnergy.MEDIUM), "어떤 작은 일을 끝냈나요?", "끝낸 일을 짧게 적어보세요"),
        action(14, "누군가에게 짧은 안부 보내기", "부담 없이 떠오르는 사람 한 명에게 짧은 안부를 보내보세요.", 120, ActionCategory.EXPRESSION, "envelope.fill", setOf(MoodEmotion.LONELY, MoodEmotion.UPSET, MoodEmotion.SAD), MoodEnergy.entries.toSet(), "누구에게 어떤 말을 보냈나요?", "예: 친구에게 잘 지내냐고 물었어요"),
        action(15, "사진첩에서 좋아하는 사진 보기", "마음에 드는 사진 한 장을 골라 천천히 바라보세요.", 120, ActionCategory.REFLECTION, "photo.fill", setOf(MoodEmotion.LONELY, MoodEmotion.UPSET, MoodEmotion.SAD), setOf(MoodEnergy.LOW), "어떤 사진이었나요?", "사진에 대한 기억을 적어보세요"),
        action(16, "스스로에게 다정한 말 건네기", "지금의 나에게 친구를 대하듯 짧고 다정한 말을 건네보세요.", 60, ActionCategory.REFLECTION, "heart.fill", setOf(MoodEmotion.UPSET, MoodEmotion.LONELY, MoodEmotion.LETHARGIC, MoodEmotion.SAD), setOf(MoodEnergy.LOW, MoodEnergy.MEDIUM), "어떤 다정한 말을 건넸나요?", "예: 오늘도 충분히 잘하고 있어"),
        action(17, "자리에서 일어나 기지개 켜기", "무리하지 않는 범위에서 팔을 뻗고 천천히 기지개를 켜보세요.", 60, ActionCategory.MOVEMENT, "figure.stand", setOf(MoodEmotion.LETHARGIC, MoodEmotion.COMPLICATED, MoodEmotion.TIRED), setOf(MoodEnergy.MEDIUM, MoodEnergy.HIGH)),
        action(18, "5분 동안 천천히 걷기", "속도를 내지 말고 발바닥의 감각을 느끼며 천천히 걸어보세요.", 300, ActionCategory.MOVEMENT, "figure.walk", setOf(MoodEmotion.ANGRY, MoodEmotion.LETHARGIC, MoodEmotion.FRUSTRATED), setOf(MoodEnergy.MEDIUM, MoodEnergy.HIGH)),
        action(19, "책상 위 물건 하나 정리하기", "눈에 보이는 작은 물건 하나만 제자리로 옮겨보세요.", 180, ActionCategory.ENVIRONMENT, "square.grid.3x3.fill", setOf(MoodEmotion.COMPLICATED, MoodEmotion.LETHARGIC, MoodEmotion.FRUSTRATED), setOf(MoodEnergy.MEDIUM), "무엇을 정리했나요?", "정리한 물건을 적어보세요"),
        action(20, "창밖 풍경 사진 한 장 찍기", "마음에 들어오는 풍경을 찾아 사진 한 장으로 남겨보세요.", 120, ActionCategory.ENVIRONMENT, "camera.fill", setOf(MoodEmotion.LONELY, MoodEmotion.COMPLICATED, MoodEmotion.SAD), MoodEnergy.entries.toSet(), "어떤 풍경이었나요?", "사진에 담은 풍경을 적어보세요"),
    )
    private fun action(n: Int, title: String, instruction: String, seconds: Int, category: ActionCategory, symbolName: String, emotions: Set<MoodEmotion>, energies: Set<MoodEnergy>, detailPrompt: String? = null, detailPlaceholder: String? = null) =
        RecoveryAction("17E3A608-17F8-4BEA-94B3-370DFBF82D%02d".format(n), title, instruction, seconds, category, symbolName, emotions, energies, n - 1, detailPrompt, detailPlaceholder)
    fun require(id: String) = actions.firstOrNull { it.id == id } ?: throw NoSuchElementException("행동을 찾을 수 없어요.")
}
