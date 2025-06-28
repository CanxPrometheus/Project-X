package com.example.smsforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smsforwarder.service.SMSService
import com.example.smsforwarder.utils.PreferencesManager

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_QUICKBOOT_POWERON,
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                
                Log.d(TAG, "Boot completed, checking if service should be started")
                
                val preferencesManager = PreferencesManager(context)
                
                // Eğer servis daha önce çalışıyorduysa, yeniden başlat
                if (preferencesManager.isServiceRunning()) {
                    Log.d(TAG, "Service was running before boot, restarting...")
                    
                    val serviceIntent = Intent(context, SMSService::class.java)
                    context.startForegroundService(serviceIntent)
                } else {
                    Log.d(TAG, "Service was not running before boot, not starting")
                }
            }
        }
    }
} 