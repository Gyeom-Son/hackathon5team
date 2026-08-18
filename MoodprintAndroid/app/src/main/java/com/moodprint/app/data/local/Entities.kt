package com.moodprint.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntryEntity(
    @PrimaryKey val id: String,
    val createdAtEpochMillis: Long,
    val emotions: List<String>,
    val energy: String,
    val note: String?,
    /** yyyy-MM-dd; 사용자가 선택한 기록 날짜. */
    val recordedLocalDate: String = "",
)

@Entity(tableName = "sync_operations", indices = [Index(value = ["sequence"], unique = true), Index("status")])
data class SyncOperationEntity(
    @PrimaryKey val id: String,
    val sequence: Long,
    val path: String,
    val bodyJson: String,
    val createdAtEpochMillis: Long,
    val status: String = "PENDING",
    val httpStatus: Int? = null,
)

@Entity(
    tableName = "action_results",
    foreignKeys = [
        ForeignKey(
            entity = MoodEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["moodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("moodId"), Index(value = ["sessionId"], unique = true)]
)
data class ActionResultEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val moodId: String,
    val actionId: String,
    val completedAtEpochMillis: Long,
    val change: String?,
    val detailNote: String?
)

@Entity(
    tableName = "rewards",
    foreignKeys = [
        ForeignKey(
            entity = ActionResultEntity::class,
            parentColumns = ["id"],
            childColumns = ["resultId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["resultId"], unique = true)]
)
data class RewardEntity(
    @PrimaryKey val id: String,
    val resultId: String,
    val experienceAwarded: Int,
    val fragmentsAwarded: Int,
    val appliedAtEpochMillis: Long
)

@Entity(
    tableName = "pet_progress",
    indices = [Index("isPrimary")]
)
data class PetProgressEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorName: String,
    val level: Int,
    val experience: Int,
    val fragments: Int,
    val requiredFragments: Int,
    val isUnlocked: Boolean,
    val isPrimary: Boolean
)

/**
 * 마음 생물 도감 16종의 고정 UUID. 참고 디자인(`mongsili_animal_growth_stages_2.html`)의
 * 동물 순서(고양이→강아지→토끼→곰→여우→판다→사자→호랑이→코알라→다람쥐→펭귄→부엉이→양→돼지→사슴→병아리)와
 * iOS(Moodprint/Core/Seed/SeedCatalog.swift)의 StablePetIDs와 동일한 값입니다.
 */
object StablePetIds {
    const val CAT = "4B0EE180-65EB-4703-89EA-F695DF421101"
    const val DOG = "4B0EE180-65EB-4703-89EA-F695DF421102"
    const val RABBIT = "4B0EE180-65EB-4703-89EA-F695DF421103"
    const val BEAR = "4B0EE180-65EB-4703-89EA-F695DF421104"
    const val FOX = "4B0EE180-65EB-4703-89EA-F695DF421105"
    const val PANDA = "4B0EE180-65EB-4703-89EA-F695DF421106"
    const val LION = "4B0EE180-65EB-4703-89EA-F695DF421107"
    const val TIGER = "4B0EE180-65EB-4703-89EA-F695DF421108"
    const val KOALA = "4B0EE180-65EB-4703-89EA-F695DF421109"
    const val SQUIRREL = "4B0EE180-65EB-4703-89EA-F695DF421110"
    const val PENGUIN = "4B0EE180-65EB-4703-89EA-F695DF421111"
    const val OWL = "4B0EE180-65EB-4703-89EA-F695DF421112"
    const val SHEEP = "4B0EE180-65EB-4703-89EA-F695DF421113"
    const val PIG = "4B0EE180-65EB-4703-89EA-F695DF421114"
    const val DEER = "4B0EE180-65EB-4703-89EA-F695DF421115"
    const val CHICK = "4B0EE180-65EB-4703-89EA-F695DF421116"
}
