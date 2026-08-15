package com.moodprint.backend.api

import com.moodprint.backend.domain.*
import jakarta.validation.constraints.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class AnonymousSessionResponse(val token: String, val tokenType: String = "Bearer")
data class CreateMoodRequest(
    val clientMoodId: UUID? = null,
    @field:Size(min = 1, max = 3) val emotions: List<MoodEmotion>,
    val energy: MoodEnergy,
    @field:Size(max = 2000) val note: String? = null,
    val recordedDate: LocalDate,
)
data class MoodResponse(val id: UUID, val emotions: Set<MoodEmotion>, val energy: MoodEnergy, val note: String?, val recordedDate: LocalDate, val createdAt: Instant)
data class ActionResponse(
    val id: String,
    val title: String,
    val instruction: String,
    val durationSeconds: Int,
    val category: ActionCategory,
    val symbolName: String,
    val supportedEmotions: Set<MoodEmotion>,
    val supportedEnergies: Set<MoodEnergy>,
    val catalogOrder: Int,
    val detailPrompt: String?,
    val detailPlaceholder: String?,
)
data class RecommendationRequest(@field:Size(min = 1, max = 3) val emotions: List<MoodEmotion>, val energy: MoodEnergy)
data class RecommendationResponse(val action: ActionResponse, val score: Double, val reason: String)
data class CompleteActionRequest(
    val sessionId: UUID,
    val moodId: UUID,
    @field:NotBlank val actionId: String,
    val change: MoodChange? = null,
    @field:Size(max = 2000) val detailNote: String? = null,
)
data class RewardResponse(val experienceAwarded: Int, val fragmentsAwarded: Int, val alreadyApplied: Boolean)
data class PetResponse(val petKey: String, val name: String, val level: Int, val experience: Int, val fragments: Int, val requiredFragments: Int, val unlocked: Boolean, val primary: Boolean)
data class CompletionResponse(val resultId: UUID, val sessionId: UUID, val reward: RewardResponse, val pets: List<PetResponse>)
data class MonthlyStatsResponse(val checkInCount: Int, val emotionCounts: Map<MoodEmotion, Int>, val energyCounts: Map<MoodEnergy, Int>, val actionCompletionCount: Int)
data class MonthlyRecordsResponse(val year: Int, val month: Int, val records: List<MoodResponse>, val stats: MonthlyStatsResponse)

fun MoodEntity.response() = MoodResponse(id, emotions, energy, note, recordedDate, createdAt)
fun RecoveryAction.response() = ActionResponse(
    id, title, instruction, durationSeconds, category, symbolName,
    emotions, energies, order, detailPrompt, detailPlaceholder,
)
fun PetProgressEntity.response() = PetResponse(petKey, name, level, experience, fragments, requiredFragments, unlocked, primaryPet)
