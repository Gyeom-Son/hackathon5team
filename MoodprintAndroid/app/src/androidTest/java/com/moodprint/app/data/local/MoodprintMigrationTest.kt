package com.moodprint.app.data.local

import android.content.Context
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MoodprintMigrationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        MoodprintDatabase::class.java,
    )

    @Test
    fun migrate1To2_preservesMoodAndBackfillsRecordedDate() {
        helper.createDatabase(DB_NAME, 1).apply {
            execSQL("INSERT INTO mood_entries (id, createdAtEpochMillis, emotions, energy, note) VALUES ('old', 1717243200000, '[\"ANXIOUS\"]', 'MEDIUM', NULL)")
            close()
        }

        helper.runMigrationsAndValidate(DB_NAME, 2, true, MoodprintDatabase.MIGRATION_1_2).use { db ->
            db.query("SELECT id, recordedLocalDate FROM mood_entries WHERE id = 'old'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("old", cursor.getString(0))
                assertTrue(cursor.getString(1).matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
            }
            db.query("SELECT COUNT(*) FROM sync_operations").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
            }
        }
    }

    @Test
    fun migrate2To3_preservesOutboxAndMarksItPending() {
        helper.createDatabase(DB_NAME, 2).apply {
            execSQL("INSERT INTO sync_operations (id, sequence, path, bodyJson, createdAtEpochMillis) VALUES ('mood:1', 1, '/moods', '{}', 1)")
            close()
        }

        helper.runMigrationsAndValidate(DB_NAME, 3, true, MoodprintDatabase.MIGRATION_2_3).use { db ->
            db.query("SELECT status, httpStatus FROM sync_operations WHERE id = 'mood:1'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("PENDING", cursor.getString(0))
                assertTrue(cursor.isNull(1))
            }
        }
    }

    private companion object { const val DB_NAME = "migration-test" }
}
