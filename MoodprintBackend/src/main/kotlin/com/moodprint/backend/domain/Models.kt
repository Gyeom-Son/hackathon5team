package com.moodprint.backend.domain

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

enum class MoodEmotion { ANXIOUS, LETHARGIC, UPSET, ANGRY, LONELY, COMPLICATED, TIRED, FRUSTRATED, SAD }
enum class MoodEnergy { LOW, MEDIUM, HIGH }
enum class MoodChange(val weight: Double) { HARDER(-0.25), SAME(0.0), BETTER(0.2), MUCH_BETTER(0.35) }
enum class ActionCategory { SENSORY, MOVEMENT, REST, EXPRESSION, REFLECTION, ENVIRONMENT }

@Entity @Table(name = "anonymous_users")
class AnonymousUser(
    @Id var id: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true, length = 64) var tokenHash: String = "",
    @Column(nullable = false) var createdAt: Instant = Instant.now(),
)

@Entity @Table(name = "moods", indexes = [Index(columnList = "owner_id,recordedDate")])
class MoodEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false) var owner: AnonymousUser = AnonymousUser(),
    @ElementCollection(fetch = FetchType.EAGER) @CollectionTable(name = "mood_emotions", joinColumns = [JoinColumn(name = "mood_id")])
    @Enumerated(EnumType.STRING) @Column(name = "emotion", nullable = false) var emotions: MutableSet<MoodEmotion> = linkedSetOf(),
    @Enumerated(EnumType.STRING) @Column(nullable = false) var energy: MoodEnergy = MoodEnergy.MEDIUM,
    @Column(length = 2000) var note: String? = null,
    @Column(nullable = false) var recordedDate: LocalDate = LocalDate.now(),
    @Column(nullable = false) var createdAt: Instant = Instant.now(),
)

@Entity @Table(name = "action_results", uniqueConstraints = [UniqueConstraint(columnNames = ["owner_id", "sessionId"])])
class ActionResultEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false) var owner: AnonymousUser = AnonymousUser(),
    @Column(nullable = false) var sessionId: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false) var mood: MoodEntity = MoodEntity(),
    @Column(nullable = false, length = 64) var actionId: String = "",
    @Enumerated(EnumType.STRING) var change: MoodChange? = null,
    @Column(length = 2000) var detailNote: String? = null,
    @Column(nullable = false) var completedAt: Instant = Instant.now(),
)

@Entity @Table(name = "rewards", uniqueConstraints = [UniqueConstraint(columnNames = ["result_id"])])
class RewardEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "result_id") var result: ActionResultEntity = ActionResultEntity(),
    @Column(nullable = false) var experience: Int = 15,
    @Column(nullable = false) var fragments: Int = 1,
    @Column(nullable = false) var appliedAt: Instant = Instant.now(),
)

@Entity @Table(name = "pet_progress", uniqueConstraints = [UniqueConstraint(columnNames = ["owner_id", "petKey"])])
class PetProgressEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY, optional = false) var owner: AnonymousUser = AnonymousUser(),
    @Column(nullable = false, length = 32) var petKey: String = "",
    @Column(nullable = false) var name: String = "",
    @Column(nullable = false) var level: Int = 0,
    @Column(nullable = false) var experience: Int = 0,
    @Column(nullable = false) var fragments: Int = 0,
    @Column(nullable = false) var requiredFragments: Int = 5,
    @Column(nullable = false) var unlocked: Boolean = false,
    @Column(nullable = false) var primaryPet: Boolean = false,
)
