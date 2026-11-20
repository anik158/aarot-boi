package com.example.util

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object LicenseClient {
    private const val BASE = "https://www.ahsanulhouqe.com"
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    fun register(state: DeviceBindingState): Boolean {
        val payload = JSONObject()
            .put("deviceId", state.deviceId)
            .put("shopName", state.shopName)
            .put("proprietorName", state.proprietorName)
            .put("phone", state.phone)
            .toString()
        val request = Request.Builder()
            .url("$BASE/api/arat/register")
            .post(payload.toRequestBody(jsonType))
            .build()
        return runCatching {
            client.newCall(request).execute().use { it.isSuccessful }
        }.getOrDefault(false)
    }

    /** approved, pending, revoked, unknown, or null when the site cannot be reached. */
    fun status(deviceId: String): String? {
        val request = Request.Builder()
            .url("$BASE/api/arat/status?deviceId=$deviceId")
            .get()
            .build()
        return runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                JSONObject(response.body?.string().orEmpty()).optString("status", "pending")
            }
        }.getOrNull()
    }
}
