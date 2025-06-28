package com.example.smsforwarder.data

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("device_token")
    val deviceToken: String,
    
    @SerializedName("created_at")
    val createdAt: String
) 