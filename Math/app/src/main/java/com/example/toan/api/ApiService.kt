// File: C:\Users\MSi\Desktop\Math\app\src\main\java\com\example\toan\api\ApiService.kt

package com.example.toan.api

import com.example.toan.model.MathRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    
    @POST("solve")
    fun solveMath(@Body request: MathRequest): Call<Map<String, Any>>
}