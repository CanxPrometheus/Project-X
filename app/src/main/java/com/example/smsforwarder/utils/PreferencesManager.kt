package com.example.smsforwarder.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "SMSForwarderPrefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_DEVICE_TOKEN = "device_token"
        private const val KEY_SERVICE_RUNNING = "service_running"
        private const val KEY_LAST_SMS_DATE = "last_sms_date"
    }
    
    fun saveDeviceToken(token: String) {
        sharedPreferences.edit().putString(KEY_DEVICE_TOKEN, token).apply()
    }
    
    fun getDeviceToken(): String? {
        return sharedPreferences.getString(KEY_DEVICE_TOKEN, null)
    }
    
    fun setServiceRunning(isRunning: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SERVICE_RUNNING, isRunning).apply()
    }
    
    fun isServiceRunning(): Boolean {
        return sharedPreferences.getBoolean(KEY_SERVICE_RUNNING, false)
    }
    
    fun saveLastSMSDate(date: String) {
        sharedPreferences.edit().putString(KEY_LAST_SMS_DATE, date).apply()
    }
    
    fun getLastSMSDate(): String? {
        return sharedPreferences.getString(KEY_LAST_SMS_DATE, null)
    }
    
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
} 