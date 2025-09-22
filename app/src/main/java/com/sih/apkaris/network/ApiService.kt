package com.sih.apkaris.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// --- Data Models ---

data class RegisterRequest(val username: String, val contact: String?, val address: String?, val password: String)
data class LoginRequest(val username: String, val password: String)
data class LocationUpdateRequest(
    val deviceId: String,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: String
)

// ADD THIS NEW DATA CLASS
data class ReportLostRequest(val deviceId: String, val userId: String)

data class LoginResponse(val success: Boolean, val token: String?, val user: User?, val message: String?)
data class User(val devices: List<Device>)
data class Device(val deviceid: String, val devicename: String, val lat: Double?, val lon: Double?)
data class GenericResponse(val success: Boolean, val message: String)


// --- API Interface ---

interface ApiService {
    @POST("register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<GenericResponse>

    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("storeLocation")
    suspend fun updateLocation(@Body request: LocationUpdateRequest): Response<GenericResponse>

    // ADD THIS NEW ENDPOINT FUNCTION
    @POST("devices/reportlost")
    suspend fun reportLostDevice(@Body request: ReportLostRequest): Response<GenericResponse>
}


// --- Retrofit Singleton Client ---

object RetrofitClient {
    private const val BASE_URL = "https://phone-lost-and-found.vercel.app/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}