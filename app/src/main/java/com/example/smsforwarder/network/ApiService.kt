package com.example.smsforwarder.network

import com.example.smsforwarder.data.SMSData
import com.example.smsforwarder.data.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    
    @POST("generate")
    suspend fun getToken(): Response<TokenResponse>
    
    @POST("api/sms/{deviceToken}")
    suspend fun forwardSMS(
        @Path("deviceToken") deviceToken: String,
        @Body smsData: SMSData
    ): Response<Unit>
} 