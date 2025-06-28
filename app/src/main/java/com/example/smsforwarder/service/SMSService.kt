package com.example.smsforwarder.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.smsforwarder.MainActivity
import com.example.smsforwarder.R
import com.example.smsforwarder.utils.PreferencesManager

class SMSService : Service() {
    
    companion object {
        private const val TAG = "SMSService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "sms_forwarder_channel"
    }
    
    private lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "SMS Service started")
        
        // Foreground servis olarak başlat
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Servis durumunu kaydet
        preferencesManager.setServiceRunning(true)
        
        return START_STICKY // Servis öldürülürse yeniden başlat
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SMS Service destroyed")
        
        // Servis durumunu kaydet
        preferencesManager.setServiceRunning(false)
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_description)
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
} 