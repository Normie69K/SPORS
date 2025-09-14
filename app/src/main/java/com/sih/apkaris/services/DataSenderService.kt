package com.sih.apkaris.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import com.sih.apkaris.models.DeviceData
import com.sih.apkaris.utils.NetworkUtils
import org.json.JSONObject

class DataSenderService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val interval = 15000L // 15 seconds
    private val serverUrl = "https://khhpmfpb-9000.inc1.devtunnels.ms/storeLocation" // Your backend

    override fun onCreate() {
        super.onCreate()
        startSendingLoop()
    }

    private fun startSendingLoop() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                sendDataToServer()
                handler.postDelayed(this, interval)
            }
        }, interval)
    }

    @SuppressLint("HardwareIds")
    private fun sendDataToServer() {
        // Collect your device data
        val deviceId = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID)
        val latitude = 0.0  // Replace with actual location
        val longitude = 0.0 // Replace with actual location
        val nearbyIds = listOf<String>() // Collect nearby device IDs from BLE scan

        val deviceData = DeviceData(deviceId, latitude, longitude, nearbyIds)

        // Convert to JSON
        val json = JSONObject().apply {
            put("deviceId", deviceData.deviceId)
            put("latitude", deviceData.latitude)
            put("longitude", deviceData.longitude)
            put("nearbyDeviceIds", deviceData.nearbyDeviceIds)
        }.toString()

        // Send to backend
        NetworkUtils.sendDataToServer(json, serverUrl, object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                // Handle failure
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.close()
            }
        })
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
