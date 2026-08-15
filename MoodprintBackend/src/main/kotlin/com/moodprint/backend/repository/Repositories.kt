package com.moodprint.backend.repository

import com.moodprint.backend.domain.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.domain.Pageable
import jakarta.persistence.LockModeType
import java.time.LocalDate
import java.util.UUID

interface AnonymousUserRepository : JpaRepository<AnonymousUser, UUID> {
    fun findByTokenHash(tokenHash: String): AnonymousUser?
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select u from AnonymousUser u where u.id=:id") fun findLocked(id: UUID): AnonymousUser?
}
interface MoodRepository : JpaRepository<MoodEntity, UUID> {
    fun findAllByOwnerIdOrderByRecordedDateDescCreatedAtDesc(ownerId: UUID): List<MoodEntity>
    fun findAllByOwnerIdAndRecordedDateBetweenOrderByRecordedDateDesc(ownerId: UUID, from: LocalDate, to: LocalDate): List<MoodEntity>
    fun findAllByOwnerIdOrderByRecordedDateDescCreatedAtDesc(ownerId: UUID, pageable: Pageable): List<MoodEntity>
    fun deleteAllByOwnerId(ownerId: UUID)
}
interface ActionResultRepository : JpaRepository<ActionResultEntity, UUID> {
    fun findByOwnerIdAndSessionId(ownerId: UUID, sessionId: UUID): ActionResultEntity?
    fun findAllByOwnerId(ownerId: UUID): List<ActionResultEntity>
    fun findAllByMoodIdIn(moodIds: Collection<UUID>): List<ActionResultEntity>
    fun existsByMoodId(moodId: UUID): Boolean
    fun deleteAllByOwnerId(ownerId: UUID)
}
interface RewardRepository : JpaRepository<RewardEntity, UUID> {
    fun findByResultId(resultId: UUID): RewardEntity?
    fun deleteAllByResultOwnerId(ownerId: UUID)
}
interface PetRepository : JpaRepository<PetProgressEntity, UUID> {
    fun findAllByOwnerIdOrderByPrimaryPetDescNameAsc(ownerId: UUID): List<PetProgressEntity>
    fun findByOwnerIdAndPrimaryPetTrue(ownerId: UUID): PetProgressEntity?
    fun deleteAllByOwnerId(ownerId: UUID)
    @Query("""
        select p from PetProgressEntity p
        where p.owner.id=:ownerId and p.primaryPet=false and p.unlocked=false
        order by case p.petKey when 'POLJJAK' then 1 when 'KKEUJEOK' then 2 else 3 end
    """)
    fun findNextLocked(ownerId: UUID): List<PetProgressEntity>
}
