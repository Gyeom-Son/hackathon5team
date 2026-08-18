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
        syncPetCatalog(db)
        db.execSQL("UPDATE pet_progress SET requiredFragments = 5 WHERE isPrimary = 0 AND isUnlocked = 0 AND requiredFragments < 5")
    }

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        syncPetCatalog(db)
    }

    /**
     * 앱 업데이트로 동물 카탈로그가 바뀌어도 기존 성장·해금 상태는 보존한다.
     * 없는 동물은 추가하고, 기존 행은 표시 이름과 동물 종류만 최신 값으로 맞춘다.
     */
    private fun syncPetCatalog(db: SupportSQLiteDatabase) {
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
            db.execSQL(
                "UPDATE pet_progress SET name = ?, colorName = ? WHERE id = ?",
                arrayOf<Any>(pet.name, pet.colorName, pet.id)
            )
        }
    }
}

/** 기본 동반자는 고양이이며, 나머지 15종은 잠금 상태로 시작해 조각을 모으며 도감을 채운다. */
private fun defaultPetProgress() = listOf(
    PetProgressEntity(StablePetIds.CAT, "고양이", "cat", 1, 0, 3, 3, true, true),
    PetProgressEntity(StablePetIds.DOG, "강아지", "dog", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.RABBIT, "토끼", "rabbit", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.BEAR, "곰", "bear", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.FOX, "여우", "fox", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.PANDA, "판다", "panda", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.LION, "사자", "lion", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.TIGER, "호랑이", "tiger", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.KOALA, "코알라", "koala", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.SQUIRREL, "다람쥐", "squirrel", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.PENGUIN, "펭귄", "penguin", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.OWL, "부엉이", "owl", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.SHEEP, "양", "sheep", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.PIG, "돼지", "pig", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.DEER, "사슴", "deer", 0, 0, 0, 5, false, false),
    PetProgressEntity(StablePetIds.CHICK, "병아리", "chick", 0, 0, 0, 5, false, false),
)
