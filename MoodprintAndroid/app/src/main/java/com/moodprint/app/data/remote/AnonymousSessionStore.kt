package com.moodprint.app.data.remote

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.anonymousSessionDataStore by preferencesDataStore("anonymous_session")

class AnonymousSessionStore(private val context: Context) {
    val deletionRequested = context.anonymousSessionDataStore.data.map { it[DELETION_REQUESTED] ?: false }
    suspend fun token(): String? = context.anonymousSessionDataStore.data.first()[TOKEN]

    suspend fun save(token: String) {
        context.anonymousSessionDataStore.edit { it[TOKEN] = token }
    }

    suspend fun clearToken() {
        context.anonymousSessionDataStore.edit { it.remove(TOKEN) }
    }

    suspend fun isDeletionRequested(): Boolean =
        context.anonymousSessionDataStore.data.first()[DELETION_REQUESTED] ?: false

    suspend fun setDeletionRequested(requested: Boolean) {
        context.anonymousSessionDataStore.edit { values ->
            if (requested) values[DELETION_REQUESTED] = true else values.remove(DELETION_REQUESTED)
        }
    }

    suspend fun clearAll() {
        context.anonymousSessionDataStore.edit { it.clear() }
    }

    suspend fun pending(): List<JSONObject> = runCatching {
        val raw = context.anonymousSessionDataStore.data.first()[PENDING] ?: "[]"
        val array = JSONArray(raw)
        List(array.length()) { array.getJSONObject(it) }
    }.getOrDefault(emptyList())

    suspend fun replacePending(operations: List<JSONObject>) {
        context.anonymousSessionDataStore.edit { preferences ->
            if (operations.isEmpty()) preferences.remove(PENDING)
            else preferences[PENDING] = JSONArray().apply { operations.forEach(::put) }.toString()
        }
    }

    suspend fun clearPending() = replacePending(emptyList())

    private companion object {
        val TOKEN = stringPreferencesKey("opaque_token")
        val PENDING = stringPreferencesKey("pending_operations")
        val DELETION_REQUESTED = booleanPreferencesKey("deletion_requested")
    }
}
