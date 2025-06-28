package com.example.smsforwarder.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import java.util.*

class SIMCardDetector(private val context: Context) {
    
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    
    /**
     * SMS'in hangi SIM kartından geldiğini tespit etmeye çalışır
     * Bu işlem cihazdan cihaza farklılık gösterebilir
     */
    fun detectSIMSlotForSMS(sender: String, message: String, timestamp: Long): Int {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return 0
        }
        
        try {
            val subscriptionInfos = subscriptionManager.activeSubscriptionInfoList
            if (subscriptionInfos != null && subscriptionInfos.isNotEmpty()) {
                
                // Eğer tek SIM varsa
                if (subscriptionInfos.size == 1) {
                    return subscriptionInfos[0].simSlotIndex
                }
                
                // Çift SIM durumunda, son SMS'leri kontrol et
                return detectFromRecentSMS(subscriptionInfos)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return 0 // Varsayılan olarak ilk SIM
    }
    
    /**
     * Son SMS'leri kontrol ederek hangi SIM'den geldiğini tespit etmeye çalışır
     */
    private fun detectFromRecentSMS(subscriptionInfos: List<SubscriptionInfo>): Int {
        // Bu yöntem cihazdan cihaza farklılık gösterebilir
        // Bazı cihazlarda SMS'in hangi SIM'den geldiği bilgisi doğrudan mevcut olmayabilir
        
        // Varsayılan olarak ilk aktif SIM'i döndür
        return subscriptionInfos.firstOrNull()?.simSlotIndex ?: 0
    }
    
    /**
     * Tüm SIM kartlarının bilgilerini alır
     */
    fun getAllSIMInfo(): List<SIMInfo> {
        val simList = mutableListOf<SIMInfo>()
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return simList
        }
        
        try {
            val subscriptionInfos = subscriptionManager.activeSubscriptionInfoList
            subscriptionInfos?.forEach { subscriptionInfo ->
                val simInfo = SIMInfo(
                    slotIndex = subscriptionInfo.simSlotIndex,
                    number = subscriptionInfo.number ?: "",
                    carrierName = subscriptionInfo.carrierName?.toString() ?: "",
                    displayName = subscriptionInfo.displayName?.toString() ?: "",
                    iccId = subscriptionInfo.iccId ?: "",
                    isActive = subscriptionInfo.isEmbedded
                )
                simList.add(simInfo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return simList
    }
    
    /**
     * Belirli bir slot'taki SIM kartının bilgilerini alır
     */
    fun getSIMInfoBySlot(slotIndex: Int): SIMInfo? {
        return getAllSIMInfo().find { it.slotIndex == slotIndex }
    }
    
    /**
     * Cihazın telefon numarasını alır
     */
    fun getDevicePhoneNumber(): String {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
            return ""
        }
        
        return try {
            telephonyManager.line1Number ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Cihazın benzersiz kimliğini alır
     */
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
    
    data class SIMInfo(
        val slotIndex: Int,
        val number: String,
        val carrierName: String,
        val displayName: String,
        val iccId: String,
        val isActive: Boolean
    )
} 