package com.sih.apkaris.network

import com.google.gson.Gson
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// --- Data Models to Match Your API ---

// For POST /register
data class RegisterRequest(
    val username: String,
    val contact: String?,
    val address: String?,
    val password: String
)

// For POST /login
data class LoginRequest(
    val username: String,
    val password: String
)

// For the response of /login
data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val user: User?,
    val message: String?
)

data class User(
    val devices: List<Device>
)

data class Device(
    val deviceid: String,
    val devicename: String,
    val lat: Double?,
    val lon: Double?
)

// A generic response for success/failure messages
data class GenericResponse(
    val success: Boolean,
    val message: String
)

// --- API Interface ---
interface ApiService {
    @POST("register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<GenericResponse>

    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>
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