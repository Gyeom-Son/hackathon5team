package com.moodprint.app.data.remote

import com.moodprint.app.BuildConfig
import com.moodprint.app.domain.MoodChange
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId

class MoodprintHttpException(
    val status: Int,
    val serverCode: String? = null,
    val retryAfterSeconds: Long? = null,
) : IOException("Moodprint server returned HTTP $status${serverCode?.let { " ($it)" }.orEmpty()}")

data class RemoteMood(
    val clientMoodId: String,
    val emotions: List<String>,
    val energy: String,
    val note: String?,
    val recordedAtEpochMillis: Long,
)

data class RemoteCompletion(
    val sessionId: String,
    val moodId: String,
    val actionId: String,
    val change: MoodChange?,
    val detailNote: String?,
)

/** Small dependency-free client. Local Room remains authoritative when the server is unavailable. */
interface MoodprintRemoteApi {
    fun createAnonymousSession(): String
    fun replay(token: String, operation: JSONObject)
    fun deleteMe(token: String)
}

class MoodprintApiClient(
    private val baseUrl: String = BuildConfig.MOODPRINT_API_BASE_URL,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : MoodprintRemoteApi {
    override fun createAnonymousSession(): String {
        val response = request("POST", "/anonymous-sessions")
        return response.getString("token")
    }

    fun uploadMood(token: String, mood: RemoteMood) {
        request("POST", "/moods", token, JSONObject().apply {
            put("clientMoodId", mood.clientMoodId)
            put("emotions", JSONArray(mood.emotions))
            put("energy", mood.energy)
            put("note", mood.note ?: JSONObject.NULL)
            put(
                "recordedDate",
                Instant.ofEpochMilli(mood.recordedAtEpochMillis).atZone(zoneId).toLocalDate().toString(),
            )
        })
    }

    fun uploadCompletion(token: String, completion: RemoteCompletion) {
        request("POST", "/action-completions", token, JSONObject().apply {
            put("sessionId", completion.sessionId)
            put("moodId", completion.moodId)
            put("actionId", completion.actionId)
            put("change", completion.change?.name ?: JSONObject.NULL)
            put("detailNote", completion.detailNote ?: JSONObject.NULL)
        })
    }

    override fun replay(token: String, operation: JSONObject) {
        request("POST", operation.getString("path"), token, operation.getJSONObject("body"))
    }

    override fun deleteMe(token: String) {
        request("DELETE", "/me", token)
    }

    private fun request(
        method: String,
        path: String,
        token: String? = null,
        body: JSONObject? = null,
    ): JSONObject {
        val connection = URL("$baseUrl$path").openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = method
            connection.connectTimeout = 3_000
            connection.readTimeout = 5_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Content-Type", "application/json")
            token?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
            if (body != null) {
                connection.doOutput = true
                connection.outputStream.bufferedWriter().use { it.write(body.toString()) }
            }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val errorCode = runCatching { JSONObject(text).optString("code").ifBlank { null } }.getOrNull()
                val retryAfter = connection.getHeaderField("Retry-After")?.toLongOrNull()
                throw MoodprintHttpException(status, errorCode, retryAfter)
            }
            if (text.isBlank()) JSONObject() else JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }
}
