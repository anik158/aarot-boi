package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class DeviceBindingState(
    val isActivated: Boolean = false,
    val shopName: String = "",
    val proprietorName: String = "",
    val phone: String = "",
    val deviceId: String = "",
    val googleAccount: String = "ahsanulhouqe321@gmail.com",
    val licenseStatus: String = "pending",
    val graceDaysRemaining: Int = 30,
    val isOfflineMode: Boolean = true,
    val canAccessOffline: Boolean = true,
    val defaultLaborCost: Double = 500.0
)

class DeviceBindingManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("arat_device_binding", Context.MODE_PRIVATE)

    private val _bindingState = MutableStateFlow(loadState())
    val bindingState: StateFlow<DeviceBindingState> = _bindingState.asStateFlow()

    private fun loadState(): DeviceBindingState {
        val licenseStatus = prefs.getString("license_status", "pending") ?: "pending"
        val shopName = prefs.getString("shop_name", "") ?: ""
        val proprietor = prefs.getString("proprietor_name", "") ?: ""
        val phone = prefs.getString("phone", "") ?: ""
        var deviceId = prefs.getString("device_id", "")
        if (deviceId.isNullOrEmpty()) {
            deviceId = "ARAT-BD-" + UUID.randomUUID().toString().take(8).uppercase()
            prefs.edit().putString("device_id", deviceId).apply()
        }
        val googleAccount = prefs.getString("google_account", "ahsanulhouqe321@gmail.com") ?: "ahsanulhouqe321@gmail.com"
        val defaultLaborCost = prefs.getString("default_labor_cost", null)?.toDoubleOrNull() ?: 500.0

        return DeviceBindingState(
            isActivated = licenseStatus == "approved",
            shopName = shopName,
            proprietorName = proprietor,
            phone = phone,
            deviceId = deviceId,
            googleAccount = googleAccount,
            licenseStatus = licenseStatus,
            graceDaysRemaining = 30,
            isOfflineMode = true,
            canAccessOffline = true,
            defaultLaborCost = defaultLaborCost
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

    fun updateRegistration(
        shopName: String,
        proprietorName: String,
        phone: String,
        googleAccount: String,
        defaultLaborCost: Double = 500.0
    ) {
        val storedLabor = if (defaultLaborCost % 1.0 == 0.0) {
            defaultLaborCost.toLong().toString()
        } else {
            defaultLaborCost.toString()
        }
        prefs.edit()
            .putString("shop_name", shopName)
            .putString("proprietor_name", proprietorName)
            .putString("phone", phone)
            .putString("google_account", googleAccount)
            .putString("default_labor_cost", storedLabor)
            .apply()

        _bindingState.value = loadState()
    }

    fun saveShopProfile(shopName: String, proprietorName: String, phone: String) {
        prefs.edit()
            .putString("shop_name", shopName.trim())
            .putString("proprietor_name", proprietorName.trim())
            .putString("phone", phone.trim())
            .apply()
        _bindingState.value = loadState()
    }

    fun setLicenseStatus(status: String) {
        prefs.edit().putString("license_status", status).apply()
        _bindingState.value = loadState()
    }

    fun isSampleLedgerCleared(): Boolean = prefs.getBoolean("sample_ledger_cleared", false)

    fun markSampleLedgerCleared() {
        prefs.edit().putBoolean("sample_ledger_cleared", true).apply()
    }
}
