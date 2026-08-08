package com.moodprint.app.util

/**
 * 한글 단어 끝의 받침 유무에 따라 알맞은 조사를 붙인다.
 * 개인화 문구처럼 사용자 데이터(닉네임, 행동 이름 등)를 문장에 끼워 넣을 때 필요하다.
 */
object KoreanParticle {
    private const val HANGUL_BASE = 0xAC00
    private const val HANGUL_LAST = 0xD7A3

    private fun hasBatchim(word: String): Boolean? {
        val last = word.trim().lastOrNull() ?: return null
        if (last.code !in HANGUL_BASE..HANGUL_LAST) return null
        return (last.code - HANGUL_BASE) % 28 != 0
    }

    /** 을/를 조사를 붙인다. 한글 음절로 끝나지 않으면 원문 그대로 반환한다. */
    fun withObjectParticle(word: String): String =
        when (hasBatchim(word)) {
            true -> "${word}을"
            false -> "${word}를"
            null -> word
        }

    /** 은/는 조사를 붙인다. 한글 음절로 끝나지 않으면 원문 그대로 반환한다. */
    fun withTopicParticle(word: String): String =
        when (hasBatchim(word)) {
            true -> "${word}은"
            false -> "${word}는"
            null -> word
        }

    /** 이/가 조사를 붙인다. 한글 음절로 끝나지 않으면 원문 그대로 반환한다. */
    fun withSubjectParticle(word: String): String =
        when (hasBatchim(word)) {
            true -> "${word}이"
            false -> "${word}가"
            null -> word
        }
}
