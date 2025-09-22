package com.sih.apkaris.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.sih.apkaris.R
import com.sih.apkaris.network.LocationUpdateRequest
import com.sih.apkaris.network.RetrofitClient
import com.sih.apkaris.utils.Logger
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class LocationService : Service() {

    companion object {
        const val CHANNEL_ID = "BleLocationChannel_v1"
        val jsonLog = MutableLiveData<String>()
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fused: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private val sendIntervalMs = 5000L

    override fun onCreate() {
        super.onCreate()
        fused = LocationServices.getFusedLocationProviderClient(this)
        createChannelAndForeground()
        startLocationUpdates()
        startSendLoop()
    }

    private fun createChannelAndForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "BLE / Location", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SPORS — Tracking Active")
            .setContentText("Broadcasting location for device safety.")
            .setSmallIcon(R.drawable.ic_ble_active)
            .setOngoing(true)
            .build()
        startForeground(1001, notification)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val req = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 3000L).build()
        fused.requestLocationUpdates(req, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                lastLocation = result.lastLocation
            }
        }, null)
    }

    private fun startSendLoop() {
        scope.launch {
            while (isActive) {
                try {
                    val loc = lastLocation
                    val request = LocationUpdateRequest(
                        deviceId = getDeviceUniqueId(),
                        latitude = loc?.latitude,
                        longitude = loc?.longitude,
                        timestamp = getTimestampISO8601()
                    )

                    val response = RetrofitClient.instance.updateLocation(request)
                    if (response.isSuccessful) {
                        Logger.d("Location updated successfully")
                    } else {
                        Logger.e("Location update failed: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    Logger.e("Location update exception", e)
                }
                delay(sendIntervalMs)
            }
        }
    }

    private fun getTimestampISO8601(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceUniqueId(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: UUID.randomUUID().toString()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}