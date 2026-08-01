package com.moodprint.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.moodprintPreferences: DataStore<Preferences> by preferencesDataStore(
    name = "moodprint_preferences"
)

data class ProfilePreferences(
    val onboardingCompleted: Boolean,
    val nickname: String,
    val loaded: Boolean = true,
)

class ProfilePreferencesRepository(context: Context) {
    private val dataStore = context.applicationContext.moodprintPreferences

    val preferences: Flow<ProfilePreferences> = dataStore.data.map { values ->
        ProfilePreferences(
            onboardingCompleted = values[ONBOARDING_COMPLETED] ?: false,
            nickname = values[NICKNAME] ?: ""
        )
    }

    suspend fun completeOnboarding(nickname: String) {
        val normalizedNickname = nickname.trim().take(MAX_NICKNAME_LENGTH)
            .ifEmpty { DEFAULT_NICKNAME }
        dataStore.edit { values ->
            values[ONBOARDING_COMPLETED] = true
            values[NICKNAME] = normalizedNickname
        }
    }

    suspend fun updateNickname(nickname: String) {
        val normalizedNickname = nickname.trim().take(MAX_NICKNAME_LENGTH)
            .ifEmpty { DEFAULT_NICKNAME }
        dataStore.edit { values -> values[NICKNAME] = normalizedNickname }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val NICKNAME = stringPreferencesKey("nickname")
        const val DEFAULT_NICKNAME = "마음 여행자"
        const val MAX_NICKNAME_LENGTH = 12
    }
}
