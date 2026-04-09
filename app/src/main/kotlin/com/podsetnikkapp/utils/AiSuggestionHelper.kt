package com.podsetnikkapp.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class AiSuggestion(
    val suggestedTitle: String,
    val suggestedDescription: String,
    val suggestedDateTimeMillis: Long,
    val explanation: String
)

object AiSuggestionHelper {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getSuggestion(userText: String, apiKey: String): Result<AiSuggestion> {
        return withContext(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val currentInfo = "Danasnji datum i vreme: ${sdf.format(Date(now))}"

                val prompt = buildString {
                    append(currentInfo)
                    append("\n\nKorisnik je napisao: \"")
                    append(userText)
                    append("\"\n\nNa osnovu teksta predlozi podsetnik. Odgovori SAMO u JSON formatu:\n")
                    append("{\n")
                    append("  \"title\": \"kratak naslov\",\n")
                    append("  \"description\": \"detaljan opis\",\n")
                    append("  \"dateTimeMillis\": 0,\n")
                    append("  \"explanation\": \"zasto si predlozio ovaj datum\"\n")
                    append("}\n")
                    append("Napomena: dateTimeMillis mora biti unix timestamp u ms u buducnosti.")
                }

                val jsonBody = JSONObject().apply {
                    put("model", "claude-haiku-4-5-20251001")
                    put("max_tokens", 500)
                    put("messages", JSONArray().put(
                        JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        }
                    ))
                }

                val request = Request.Builder()
                    .url("https://api.anthropic.com/v1/messages")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: return@withContext Result.failure(Exception("Prazan odgovor"))

                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("API greska: ${response.code}"))
                }

                val responseJson = JSONObject(responseBody)
                val content = responseJson.getJSONArray("content").getJSONObject(0).getString("text").trim()
                val cleanJson = content.replace("```json", "").replace("```", "").trim()
                val suggestionJson = JSONObject(cleanJson)

                Result.success(AiSuggestion(
                    suggestedTitle = suggestionJson.optString("title", userText),
                    suggestedDescription = suggestionJson.optString("description", ""),
                    suggestedDateTimeMillis = suggestionJson.optLong("dateTimeMillis", now + 3600000),
                    explanation = suggestionJson.optString("explanation", "")
                ))
            } catch (e: Exception) {
                Log.e("AiSuggestion", "Greska: ${e.message}")
                Result.failure(e)
            }
        }
    }
}
