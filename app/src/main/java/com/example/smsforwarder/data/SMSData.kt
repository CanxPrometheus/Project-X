package com.example.smsforwarder.data

import com.google.gson.annotations.SerializedName

data class SMSData(
    @SerializedName("sender")
    val sender: String,
    
    @SerializedName("receiver")
    val receiver: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("simSlot")
    val simSlot: Int = 0,
    
    @SerializedName("deviceToken")
    val deviceToken: String
) 