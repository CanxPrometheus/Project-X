package com.example.smsforwarder.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import java.util.*

class SIMCardUtils(private val context: Context) {
    
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    
    fun getSIMCardInfo(): List<SIMCardInfo> {
        val simCards = mutableListOf<SIMCardInfo>()
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return simCards
        }
        
        try {
            val subscriptionInfos = subscriptionManager.activeSubscriptionInfoList
            subscriptionInfos?.forEach { subscriptionInfo ->
                val simCardInfo = SIMCardInfo(
                    slotIndex = subscriptionInfo.simSlotIndex,
                    number = subscriptionInfo.number ?: "",
                    carrierName = subscriptionInfo.carrierName?.toString() ?: "",
                    displayName = subscriptionInfo.displayName?.toString() ?: "",
                    iccId = subscriptionInfo.iccId ?: ""
                )
                simCards.add(simCardInfo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return simCards
    }
    
    fun getPhoneNumber(): String {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
            return ""
        }
        
        return try {
            telephonyManager.line1Number ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    
    fun getDeviceId(): String {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return ""
        }
        
        return try {
            telephonyManager.deviceId ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    
    data class SIMCardInfo(
        val slotIndex: Int,
        val number: String,
        val carrierName: String,
        val displayName: String,
        val iccId: String
    )
} 