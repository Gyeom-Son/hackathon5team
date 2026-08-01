package com.moodprint.app.data.local

import androidx.room.TypeConverter

internal class MoodprintTypeConverters {
    @TypeConverter
    fun emotionsToStorage(value: List<String>): String = value.joinToString(SEPARATOR)

    @TypeConverter
    fun emotionsFromStorage(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(SEPARATOR)

    private companion object {
        const val SEPARATOR = "\u001F"
    }
}
