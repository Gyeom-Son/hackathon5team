package com.moodprint.backend.service

import com.moodprint.backend.api.*
import com.moodprint.backend.config.hashToken
import com.moodprint.backend.domain.*
import com.moodprint.backend.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.PageRequest
import java.security.SecureRandom
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.util.Base64
import java.util.UUID

class NotFound(message: String) : RuntimeException(message)
class Conflict(message: String) : RuntimeException(message)
class RateLimited(message: String) : RuntimeException(message)

@Service
class SessionService(private val users: AnonymousUserRepository, private val pets: PetRepository) {
    @Transactional
    fun create(): AnonymousSessionResponse {
        val bytes = ByteArray(32).also(SecureRandom()::nextBytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        val user = users.save(AnonymousUser(tokenHash = hashToken(token)))
        pets.saveAll(defaultPets(user))
        return AnonymousSessionResponse(token)
    }
    private fun defaultPets(owner: AnonymousUser) = listOf(
        PetProgressEntity(owner = owner, petKey = "MONGSIL", name = "몽실이", level = 1, fragments = 3, requiredFragments = 3, unlocked = true, primaryPet = true),
        PetProgressEntity(owner = owner, petKey = "POLJJAK", name = "폴짝이"),
        PetProgressEntity(owner = owner, petKey = "KKEUJEOK", name = "끄적이"),
        PetProgressEntity(owner = owner, petKey = "BANJJAK", name = "반짝이"),
    )
}

@Service
class MoodService(
    private val moods: MoodRepository,
    private val results: ActionResultRepository,
    private val clock: Clock,
) {
    @Transactional
    fun create(owner: AnonymousUser, request: CreateMoodRequest): MoodResponse {
        if (request.emotions.distinct().size != request.emotions.size) throw IllegalArgumentException("같은 감정을 중복할 수 없어요.")
        if (request.recordedDate.isAfter(LocalDate.now(clock))) throw IllegalArgumentException("미래 날짜에는 기록할 수 없어요.")
        val id = request.clientMoodId ?: UUID.randomUUID()
        moods.findById(id).orElse(null)?.let {
            if (it.owner.id != owner.id) throw Conflict("이미 사용 중인 clientMoodId예요.")
            val note = request.note?.trim()?.ifBlank { null }
            if (it.emotions != request.emotions.toSet() || it.energy != request.energy || it.note != note || it.recordedDate != request.recordedDate) {
                if (results.existsByMoodId(it.id)) throw Conflict("완료된 행동이 있는 마음 기록은 변경할 수 없어요.")
                it.emotions.clear()
                it.emotions.addAll(request.emotions)
                it.energy = request.energy
                it.note = note
                it.recordedDate = request.recordedDate
            }
            return it.response()
        }
        return moods.save(MoodEntity(id, owner, request.emotions.toMutableSet(), request.energy, request.note?.trim()?.ifBlank { null }, request.recordedDate)).response()
    }
    @Transactional(readOnly = true) fun all(owner: AnonymousUser) = moods.findAllByOwnerIdOrderByRecordedDateDescCreatedAtDesc(owner.id).map { it.response() }
    @Transactional(readOnly = true) fun limited(owner: AnonymousUser, page: Int, size: Int) =
        moods.findAllByOwnerIdOrderByRecordedDateDescCreatedAtDesc(owner.id, PageRequest.of(page, size)).map { it.response() }
    @Transactional(readOnly = true)
    fun monthly(owner: AnonymousUser, year: Int, month: Int): MonthlyRecordsResponse {
        val ym = try { YearMonth.of(year, month) } catch (_: Exception) { throw IllegalArgumentException("유효한 연도와 월을 입력해 주세요.") }
        val records = moods.findAllByOwnerIdAndRecordedDateBetweenOrderByRecordedDateDesc(owner.id, ym.atDay(1), ym.atEndOfMonth())
        val completedMoodIds = results.findAllByMoodIdIn(records.map { it.id }).map { it.mood.id }.toSet()
        val emotionCounts = records.flatMap { it.emotions }.groupingBy { it }.eachCount()
        val energyCounts = records.groupingBy { it.energy }.eachCount()
        return MonthlyRecordsResponse(year, month, records.map { it.response() }, MonthlyStatsResponse(records.size, emotionCounts, energyCounts, completedMoodIds.size))
    }
}

@Service
class RecommendationService(private val results: ActionResultRepository) {
    @Transactional(readOnly = true)
    fun recommend(owner: AnonymousUser, input: RecommendationRequest): List<RecommendationResponse> {
        if (input.emotions.distinct().size != input.emotions.size) throw IllegalArgumentException("같은 감정을 중복할 수 없어요.")
        val selectedEmotions = input.emotions.toSet()
        val history = results.findAllByOwnerId(owner.id)
        return ActionCatalog.actions.map { action ->
            val emotionMatches = action.emotions.intersect(selectedEmotions).size
            val energyMatch = input.energy in action.energies
            val contextual = history.filter { result ->
                result.actionId == action.id && result.mood.energy == input.energy && result.mood.emotions.intersect(selectedEmotions).isNotEmpty()
            }
            val changes = contextual.mapNotNull { it.change }
            val average = changes.map { it.weight }.average().takeUnless { it.isNaN() } ?: 0.0
            val score = emotionMatches * 10.0 + if (energyMatch) 5.0 else -4.0 + average * 3.0 - contextual.size.coerceAtMost(5) * .5
            val base = when {
                emotionMatches > 0 && energyMatch -> "선택한 감정 ${emotionMatches}개와 에너지에 맞는 행동이에요."
                emotionMatches > 0 -> "선택한 감정 ${emotionMatches}개를 고려한 행동이에요."
                energyMatch -> "지금 에너지에 맞는 행동이에요."
                else -> "지금 부담이 적은 행동이에요."
            }
            val positive = contextual.count { it.change == MoodChange.BETTER || it.change == MoodChange.MUCH_BETTER }
            val reason = if (positive > 0) "$base 비슷한 기록에서 ${positive}번 도움이 되었다고 남겼어요." else base
            RecommendationResponse(action.response(), score, reason)
        }.sortedWith(compareByDescending<RecommendationResponse> { it.score }.thenBy { it.action.id })
    }
}

@Service
class CompletionService(
    private val users: AnonymousUserRepository, private val moods: MoodRepository, private val results: ActionResultRepository,
    private val rewards: RewardRepository, private val pets: PetRepository,
) {
    @Transactional
    fun complete(ownerInput: AnonymousUser, request: CompleteActionRequest): CompletionResponse {
        val owner = users.findLocked(ownerInput.id) ?: throw NotFound("익명 세션을 찾을 수 없어요.")
        results.findByOwnerIdAndSessionId(owner.id, request.sessionId)?.let {
            val detail = request.detailNote?.trim()?.ifBlank { null }
            if (it.mood.id != request.moodId || it.actionId != request.actionId || it.change != request.change || it.detailNote != detail) {
                throw Conflict("같은 sessionId에 다른 완료 결과를 저장할 수 없어요.")
            }
            return response(it, true)
        }
        val mood = moods.findById(request.moodId).orElseThrow { NotFound("마음 기록을 찾을 수 없어요.") }
        if (mood.owner.id != owner.id) throw NotFound("마음 기록을 찾을 수 없어요.")
        ActionCatalog.require(request.actionId)
        val result = results.save(ActionResultEntity(owner = owner, sessionId = request.sessionId, mood = mood, actionId = request.actionId, change = request.change, detailNote = request.detailNote?.trim()?.ifBlank { null }))
        val primary = pets.findByOwnerIdAndPrimaryPetTrue(owner.id) ?: throw IllegalStateException("기본 펫이 없어요.")
        primary.experience += 15; primary.level = primary.experience / 100 + 1
        val collection = pets.findNextLocked(owner.id).firstOrNull()
        val fragments = if (collection == null) 0 else 1
        collection?.let { it.fragments = minOf(it.requiredFragments, it.fragments + 1); if (it.fragments >= it.requiredFragments) it.unlocked = true }
        rewards.save(RewardEntity(result = result, experience = 15, fragments = fragments))
        return response(result, false)
    }
    private fun response(result: ActionResultEntity, already: Boolean): CompletionResponse {
        val reward = rewards.findByResultId(result.id) ?: throw IllegalStateException("보상을 찾을 수 없어요.")
        return CompletionResponse(result.id, result.sessionId, RewardResponse(reward.experience, reward.fragments, already), pets.findAllByOwnerIdOrderByPrimaryPetDescNameAsc(result.owner.id).map { it.response() })
    }
}

@Service
class AccountService(
    private val users: AnonymousUserRepository,
    private val moods: MoodRepository,
    private val results: ActionResultRepository,
    private val rewards: RewardRepository,
    private val pets: PetRepository,
) {
    @Transactional
    fun delete(ownerInput: AnonymousUser) {
        val owner = users.findLocked(ownerInput.id) ?: throw NotFound("익명 세션을 찾을 수 없어요.")
        rewards.deleteAllByResultOwnerId(owner.id)
        results.deleteAllByOwnerId(owner.id)
        pets.deleteAllByOwnerId(owner.id)
        moods.deleteAllByOwnerId(owner.id)
        users.delete(owner)
    }
}
