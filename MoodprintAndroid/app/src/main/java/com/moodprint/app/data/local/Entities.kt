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
    val note: String?
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

object StablePetIds {
    const val MONGSIL = "4B0EE180-65EB-4703-89EA-F695DF421101"
    const val POLJJAK = "4B0EE180-65EB-4703-89EA-F695DF421102"
    const val KKEUJEOK = "4B0EE180-65EB-4703-89EA-F695DF421103"
    const val BANJJAK = "4B0EE180-65EB-4703-89EA-F695DF421104"
}
