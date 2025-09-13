package com.sih.apkaris.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.sih.apkaris.R
import com.sih.apkaris.bluetooth.BluetoothScanner
import com.sih.apkaris.models.BlePayload
import com.sih.apkaris.models.NearbyDevice
import com.sih.apkaris.utils.Logger
import com.sih.apkaris.utils.NetworkUtils
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class LocationService : Service() {

    companion object {
        const val CHANNEL_ID = "BleLocationChannel_v1"
        val jsonLog = MutableLiveData<String>()
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var fused: FusedLocationProviderClient
    private var lastLocation: Location? = null

    private lateinit var scanner: BluetoothScanner

    private val sendIntervalMs = 5000L
    private val serverUrl = "https://khhpmfpb-9000.inc1.devtunnels.ms/"

    override fun onCreate() {
        super.onCreate()
        Logger.d("LocationService onCreate")
        fused = LocationServices.getFusedLocationProviderClient(this)
        scanner = BluetoothScanner(this)

        createChannelAndForeground()
        startLocationUpdates()
        scanner.startScanning()
        startSendLoop()
    }

    private fun createChannelAndForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BLE / Location",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("APkARiS — Tracking")
            .setContentText("Sending BLE & location data")
            .setSmallIcon(R.drawable.ic_ble_active)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Logger.e("No location permission, stopping updates")
            return
        }

        val req = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, 3000L
        )
            .setMinUpdateIntervalMillis(2000L)
            .build()

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
                    val deviceId = getDeviceUniqueId()
                    val loc = lastLocation
                    val nearby = scanner.snapshotNearby().map {
                        NearbyDevice(it.first, it.second)
                    }

                    val payload = BlePayload(
                        deviceId = deviceId,
                        latitude = loc?.latitude,
                        longitude = loc?.longitude,
                        timestamp = System.currentTimeMillis(),
                        nearby = nearby
                    )

                    val json = buildJson(payload)

                    withContext(Dispatchers.IO) {
                        NetworkUtils.sendDataToServer(json, serverUrl, object : okhttp3.Callback {
                            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                                LocationService.jsonLog.postValue("Server FAILED: ${e.message}\n$json\n")
                            }

                            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                                val body = response.body?.string() ?: ""
                                LocationService.jsonLog.postValue("Server SUCCESS: $body\n$json\n")
                                response.close()
                            }
                        })
                    }

                    scanner.clearSnapshot()
                } catch (t: Throwable) {
                    jsonLog.postValue("sendLoop error: ${t.localizedMessage}")
                }

                delay(sendIntervalMs)
            }
        }
    }


    private fun buildJson(p: BlePayload): String {
        val root = JSONObject()
        root.put("deviceId", p.deviceId)
        root.put("timestamp", p.timestamp)
        if (p.latitude != null && p.longitude != null) {
            root.put("latitude", p.latitude)
            root.put("longitude", p.longitude)
        } else {
            root.put("latitude", JSONObject.NULL)
            root.put("longitude", JSONObject.NULL)
        }
        val arr = JSONArray()
        for (n in p.nearby) {
            val o = JSONObject()
            o.put("id", n.id)
            o.put("rssi", n.rssi)
            arr.put(o)
        }
        root.put("nearby", arr)
        return root.toString()
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceUniqueId(): String {
        return Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: UUID.randomUUID().toString()
    }

    override fun onDestroy() {
        scope.cancel()
        try {
            scanner.stopScanning()
        } catch (_: Throwable) {
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
