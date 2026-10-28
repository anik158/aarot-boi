package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class DeviceBindingState(
    val isActivated: Boolean = true,
    val shopName: String = "মেসার্স চালের আরত",
    val proprietorName: String = "মো: রফিকুল ইসলাম",
    val phone: String = "01712-345678",
    val deviceId: String = "ARAT-BD-7989-9266",
    val googleAccount: String = "ahsanulhouqe321@gmail.com",
    val licenseStatus: String = "সক্রিয় (Active License)",
    val graceDaysRemaining: Int = 30,
    val isOfflineMode: Boolean = true,
    val canAccessOffline: Boolean = true
)

class DeviceBindingManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("arat_device_binding", Context.MODE_PRIVATE)

    private val _bindingState = MutableStateFlow(loadState())
    val bindingState: StateFlow<DeviceBindingState> = _bindingState.asStateFlow()

    private fun loadState(): DeviceBindingState {
        val activated = prefs.getBoolean("is_activated", true)
        val shopName = prefs.getString("shop_name", "মেসার্স চালের আরত") ?: "মেসার্স চালের আরত"
        val proprietor = prefs.getString("proprietor_name", "মো: রফিকুল ইসলাম") ?: "মো: রফিকুল ইসলাম"
        val phone = prefs.getString("phone", "01712-345678") ?: "01712-345678"
        var deviceId = prefs.getString("device_id", "")
        if (deviceId.isNullOrEmpty()) {
            deviceId = "ARAT-BD-" + UUID.randomUUID().toString().take(8).uppercase()
            prefs.edit().putString("device_id", deviceId).apply()
        }
        val googleAccount = prefs.getString("google_account", "ahsanulhouqe321@gmail.com") ?: "ahsanulhouqe321@gmail.com"

        return DeviceBindingState(
            isActivated = activated,
            shopName = shopName,
            proprietorName = proprietor,
            phone = phone,
            deviceId = deviceId,
            googleAccount = googleAccount,
            licenseStatus = "সক্রিয় (Active License)",
            graceDaysRemaining = 30,
            isOfflineMode = true,
            canAccessOffline = true
        )
    }

    fun loadCashCounts(): IntArray {
        val notes = intArrayOf(1000, 500, 200, 100, 50, 20, 10)
        return IntArray(notes.size) { index -> prefs.getInt("cash_note_${notes[index]}", 0) }
    }

    fun saveCashCounts(counts: IntArray) {
        val notes = intArrayOf(1000, 500, 200, 100, 50, 20, 10)
        val editor = prefs.edit()
        notes.forEachIndexed { index, note ->
            editor.putInt("cash_note_$note", counts.getOrElse(index) { 0 })
        }
        editor.apply()
    }

    fun updateRegistration(shopName: String, proprietorName: String, phone: String, googleAccount: String) {
        prefs.edit()
            .putBoolean("is_activated", true)
            .putString("shop_name", shopName)
            .putString("proprietor_name", proprietorName)
            .putString("phone", phone)
            .putString("google_account", googleAccount)
            .apply()

        _bindingState.value = loadState()
    }
}
