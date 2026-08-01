package com.moodprint.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MoodEntryEntity::class,
        ActionResultEntity::class,
        RewardEntity::class,
        PetProgressEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(MoodprintTypeConverters::class)
abstract class MoodprintDatabase : RoomDatabase() {
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun actionResultDao(): ActionResultDao
    abstract fun rewardDao(): RewardDao
    abstract fun petProgressDao(): PetProgressDao
    abstract fun completionRewardDao(): CompletionRewardDao

    companion object {
        private const val DATABASE_NAME = "moodprint.db"

        @Volatile
        private var instance: MoodprintDatabase? = null

        fun getInstance(context: Context): MoodprintDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MoodprintDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(PetSeedCallback)
                    .build()
                    .also { instance = it }
            }
    }
}

private object PetSeedCallback : RoomDatabase.Callback() {
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("UPDATE pet_progress SET requiredFragments = 5 WHERE isPrimary = 0 AND isUnlocked = 0 AND requiredFragments < 5")
    }

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        defaultPets.forEach { pet ->
            db.execSQL(
                """
                INSERT OR IGNORE INTO pet_progress
                (id, name, colorName, level, experience, fragments, requiredFragments, isUnlocked, isPrimary)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf<Any>(
                    pet.id,
                    pet.name,
                    pet.colorName,
                    pet.level,
                    pet.experience,
                    pet.fragments,
                    pet.requiredFragments,
                    if (pet.isUnlocked) 1 else 0,
                    if (pet.isPrimary) 1 else 0
                )
            )
        }
    }

    private val defaultPets = listOf(
        PetProgressEntity(StablePetIds.MONGSIL, "몽실이", "lavender", 1, 0, 3, 3, true, true),
        PetProgressEntity(StablePetIds.POLJJAK, "폴짝이", "mint", 0, 0, 0, 5, false, false),
        PetProgressEntity(StablePetIds.KKEUJEOK, "끄적이", "coral", 0, 0, 0, 5, false, false),
        PetProgressEntity(StablePetIds.BANJJAK, "반짝이", "yellow", 0, 0, 0, 5, false, false)
    )
}
