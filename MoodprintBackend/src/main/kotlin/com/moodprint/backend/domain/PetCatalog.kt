package com.moodprint.backend.domain

/** Android 앱과 공유하는 MVP 도감 순서의 단일 원본. */
object PetCatalog {
    data class Seed(val key: String, val name: String)

    val entries = listOf(
        Seed("cat", "고양이"),
        Seed("dog", "강아지"),
        Seed("rabbit", "토끼"),
        Seed("bear", "곰"),
        Seed("fox", "여우"),
        Seed("panda", "판다"),
        Seed("lion", "사자"),
        Seed("tiger", "호랑이"),
        Seed("koala", "코알라"),
        Seed("squirrel", "다람쥐"),
        Seed("penguin", "펭귄"),
        Seed("owl", "부엉이"),
        Seed("sheep", "양"),
        Seed("pig", "돼지"),
        Seed("deer", "사슴"),
        Seed("chick", "병아리"),
    )

    private val orderByKey = entries.mapIndexed { index, seed -> seed.key to index }.toMap()

    fun sorted(pets: Collection<PetProgressEntity>): List<PetProgressEntity> =
        pets.sortedBy { orderByKey[it.petKey] ?: Int.MAX_VALUE }

    fun nextLocked(pets: Collection<PetProgressEntity>): PetProgressEntity? =
        sorted(pets).firstOrNull { !it.primaryPet && !it.unlocked }
}
