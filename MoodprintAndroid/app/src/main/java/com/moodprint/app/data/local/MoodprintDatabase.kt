package com.moodprint.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import androidx.room.withTransaction

@Database(
    entities = [
        MoodEntryEntity::class,
        ActionResultEntity::class,
        RewardEntity::class,
        PetProgressEntity::class,
        SyncOperationEntity::class,
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(MoodprintTypeConverters::class)
abstract class MoodprintDatabase : RoomDatabase() {
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun actionResultDao(): ActionResultDao
    abstract fun rewardDao(): RewardDao
    abstract fun petProgressDao(): PetProgressDao
    abstract fun completionRewardDao(): CompletionRewardDao
    abstract fun syncOperationDao(): SyncOperationDao

    suspend fun resetUserData() = withTransaction {
        syncOperationDao().clear()
        moodEntryDao().deleteAll() // Cascades action results and rewards.
        petProgressDao().deleteAll()
        petProgressDao().insertAll(defaultPetProgress())
    }

    companion object {
        private const val DATABASE_NAME = "moodprint.db"

        @Volatile
        private var instance: MoodprintDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE mood_entries ADD COLUMN recordedLocalDate TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE mood_entries SET recordedLocalDate = strftime('%Y-%m-%d', createdAtEpochMillis / 1000, 'unixepoch', 'localtime')")
                db.execSQL("CREATE TABLE IF NOT EXISTS sync_operations (id TEXT NOT NULL PRIMARY KEY, sequence INTEGER NOT NULL, path TEXT NOT NULL, bodyJson TEXT NOT NULL, createdAtEpochMillis INTEGER NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_sync_operations_sequence ON sync_operations(sequence)")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sync_operations ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("ALTER TABLE sync_operations ADD COLUMN httpStatus INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_operations_status ON sync_operations(status)")
            }
        }

        fun getInstance(context: Context): MoodprintDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MoodprintDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
        defaultPetProgress().forEach { pet ->
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

}

private fun defaultPetProgress() = listOf(
    PetProgressEntity(StablePetIds.MONGSIL, "몽실이", "lavender", 1, 0, 3, 3, true, true),
    PetProgressEntity(StablePetIds.POLJJAK, "폴짝이", "mint", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.KKEUJEOK, "끄적이", "coral", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.BANJJAK, "반짝이", "yellow", 0, 0, 0, 5, false, false),
)
