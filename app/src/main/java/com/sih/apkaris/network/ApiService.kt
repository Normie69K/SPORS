package com.sih.apkaris.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// --- Data Models ---

// Represents a single point in the location history from the 'data' array
data class TrackPoint(
    val latitude: String,
    val longitude: String,
    val timestamp: String
)

// The response model for the /getlocation endpoint
data class DeviceHistoryResponse(
    val success: Boolean,
    val data: List<TrackPoint>?,
    val message: String?
)

data class RegisterRequest(
    val username: String,
    val contact: String?,
    val address: String?,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

// Added `nearby` parameter to support broadcasting nearby scanned devices
data class LocationUpdateRequest(
    val deviceId: String,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: String,
    val nearby: List<NearbyDevice>? = null
)

data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val user: User?,
    val message: String?
)

data class User(val devices: List<Device>)

data class Device(
    val deviceid: String,
    val devicename: String,
    val lat: Double?,
    val lon: Double?
)

data class GenericResponse(
    val success: Boolean,
    val message: String
)

// Model for nearby scanned devices
data class NearbyDevice(
    val id: String,
    val rssi: Int
)


// --- API Interface ---
interface ApiService {
    @POST("register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<GenericResponse>

    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("storeLocation")
    suspend fun updateLocation(@Body request: LocationUpdateRequest): Response<GenericResponse>

    @POST("reportlost/{deviceId}")
    suspend fun reportLostDevice(@Path("deviceId") deviceId: String): Response<GenericResponse>

    @GET("getlocation/{deviceId}")
    suspend fun getDeviceLocationHistory(@Path("deviceId") deviceId: String): Response<DeviceHistoryResponse>
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
