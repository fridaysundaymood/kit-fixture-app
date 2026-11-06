package dev.androidkit.fixture.data

import android.content.Context
import dev.androidkit.fixture.BuildConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

class SyncClient(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("sync", Context.MODE_PRIVATE)

    fun setDebugOverride(url: String, anonKey: String) {
        check(BuildConfig.DEBUG) { "Endpoint overrides require a debug build" }
        preferences.edit().putString("url", url.trim()).putString("key", anonKey.trim())
            .remove("refresh_token").apply()
    }

    fun clearDebugOverride() {
        check(BuildConfig.DEBUG) { "Endpoint overrides require a debug build" }
        preferences.edit().remove("url").remove("key").remove("refresh_token").apply()
    }

    fun sync(
        boards: List<Board>,
        tasks: List<Task>,
        statusForSync: (TaskStatus) -> String = { it.id },
    ) {
        val url = (if (BuildConfig.DEBUG) preferences.getString("url", null) else null)
            .takeUnless { it.isNullOrBlank() } ?: BuildConfig.SUPABASE_URL
        val key = (if (BuildConfig.DEBUG) preferences.getString("key", null) else null)
            .takeUnless { it.isNullOrBlank() } ?: BuildConfig.SUPABASE_ANON_KEY
        require(url.isNotBlank() && key.isNotBlank()) { "Supabase URL and anonymous key are required" }
        val refreshToken = preferences.getString("refresh_token", null)
        val authPath = if (refreshToken == null) "/auth/v1/signup" else "/auth/v1/token?grant_type=refresh_token"
        val authBody = if (refreshToken == null) JSONObject() else JSONObject().put("refresh_token", refreshToken)
        val auth = JSONObject(request(url, authPath, key, key, authBody.toString()))
        val accessToken = auth.getString("access_token")
        check(preferences.edit().putString("refresh_token", auth.getString("refresh_token")).commit())

        val boardIds = boards.associate { it.id to it.remoteId }
        val boardRows = JSONArray()
        boards.forEach { boardRows.put(JSONObject().put("id", it.remoteId).put("name", it.name)) }
        val taskRows = JSONArray()
        tasks.forEach {
            val boardId = requireNotNull(boardIds[it.boardId]) { "Task references an unknown board" }
            taskRows.put(JSONObject().put("id", it.remoteId).put("board_id", boardId)
                .put("title", it.title).put("status", if (BuildConfig.FLAVOR == "defects") statusForSync(it.status) else it.status.id)
                .put("priority", it.priority.id).put("completed", it.status == TaskStatus.Done))
        }
        if (boardRows.length() > 0) request(url, "/rest/v1/boards", key, accessToken, boardRows.toString(), "resolution=ignore-duplicates,return=minimal")
        if (taskRows.length() > 0) request(url, "/rest/v1/tasks", key, accessToken, taskRows.toString(), "resolution=merge-duplicates,return=minimal")
    }

    private fun request(base: String, path: String, key: String, token: String, body: String, prefer: String? = null): String {
        val connection = URL(base.trimEnd('/') + path).openConnection() as HttpURLConnection
        try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("apikey", key)
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Content-Type", "application/json")
            if (prefer != null) connection.setRequestProperty("Prefer", prefer)
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            if (code !in 200..299) throw IOException("Supabase request failed ($code)")
            return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
