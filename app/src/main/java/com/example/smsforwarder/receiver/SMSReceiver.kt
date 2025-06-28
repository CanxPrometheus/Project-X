package com.example.smsforwarder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.example.smsforwarder.data.SMSData
import com.example.smsforwarder.network.RetrofitClient
import com.example.smsforwarder.utils.PreferencesManager
import com.example.smsforwarder.utils.SIMCardDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SMSReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "SMSReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val simCardDetector = SIMCardDetector(context)
            val preferencesManager = PreferencesManager(context)
            
            messages?.forEach { smsMessage ->
                val sender = smsMessage.originatingAddress ?: "Unknown"
                val messageBody = smsMessage.messageBody ?: ""
                val timestamp = smsMessage.timestampMillis
                
                // SIM kart bilgilerini al
                val receiver = simCardDetector.getDevicePhoneNumber()
                val deviceToken = preferencesManager.getDeviceToken()
                
                // SMS'in hangi SIM'den geldiğini tespit et
                val simSlot = simCardDetector.detectSIMSlotForSMS(sender, messageBody, timestamp)
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = dateFormat.format(Date(timestamp))
                
                Log.d(TAG, "SMS received from: $sender, message: $messageBody, date: $date, simSlot: $simSlot")
                
                // SIM kart bilgilerini logla
                val simInfo = simCardDetector.getSIMInfoBySlot(simSlot)
                if (simInfo != null) {
                    Log.d(TAG, "SIM Info - Slot: ${simInfo.slotIndex}, Number: ${simInfo.number}, Carrier: ${simInfo.carrierName}")
                }
                
                if (deviceToken != null) {
                    val smsData = SMSData(
                        sender = sender,
                        receiver = receiver,
                        message = messageBody,
                        date = date,
                        simSlot = simSlot,
                        deviceToken = deviceToken
                    )
                    
                    // SMS'i sunucuya gönder (cihazın token'ı ile)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = RetrofitClient.apiService.forwardSMS(deviceToken, smsData)
                            if (response.isSuccessful) {
                                Log.d(TAG, "SMS forwarded successfully to server")
                                preferencesManager.saveLastSMSDate(date)
                            } else {
                                Log.e(TAG, "Failed to forward SMS: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error forwarding SMS", e)
                        }
                    }
                } else {
                    Log.e(TAG, "Device token is null, cannot forward SMS")
                }
            }
        }
    }
} 