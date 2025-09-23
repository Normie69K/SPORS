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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.sih.apkaris.R
import com.sih.apkaris.bluetooth.BluetoothScanner
import com.sih.apkaris.bluetooth.ScannedDevice
import com.sih.apkaris.network.LocationUpdateRequest
import com.sih.apkaris.network.NearbyDevice
import com.sih.apkaris.network.RetrofitClient
import com.sih.apkaris.utils.Logger
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class LocationService : Service() {

    companion object {
        const val ACTION_DEVICES_UPDATE = "com.sih.apkaris.services.ACTION_DEVICES_UPDATE"
        const val EXTRA_DEVICES = "com.sih.apkaris.services.EXTRA_DEVICES"

        const val CHANNEL_ID = "LocationServiceChannel"
        private const val NOTIFICATION_ID = 1001
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fused: FusedLocationProviderClient
    private var lastLocation: Location? = null

    private lateinit var scanner: BluetoothScanner
    private val sendIntervalMs = 5000L

    override fun onCreate() {
        super.onCreate()
        fused = LocationServices.getFusedLocationProviderClient(this)
        scanner = BluetoothScanner(this)
        createChannelAndForeground()
        startLocationUpdates()
        scanner.startScanning()
        startSendLoop()
    }

    private fun startSendLoop() {
        scope.launch {
            while (isActive) {
                try {
                    // 1. Get a snapshot of ALL nearby devices
                    val allNearbyDevices = scanner.snapshotNearby()

                    // 2. Filter devices to only include ones with required name prefix
                    val filteredDevices = allNearbyDevices.filter { device ->
                        device.name?.startsWith("SIH_TEAM_SAPPHIRE") == true
                    }

                    // 3. Send the filtered list to the UI
                    broadcastDevices(ArrayList(filteredDevices))

                    // 4. Map filtered list into NearbyDevice objects
                    val nearbyPayload = filteredDevices.map { device ->
                        NearbyDevice(id = device.address, rssi = device.rssi)
                    }

                    val loc = lastLocation
                    val request = LocationUpdateRequest(
                        deviceId = getDeviceUniqueId(),
                        latitude = loc?.latitude,
                        longitude = loc?.longitude,
                        timestamp = getTimestampISO8601(),
                        nearby = nearbyPayload
                    )

                    val response = RetrofitClient.instance.updateLocation(request)
                    if (response.isSuccessful) {
                        Logger.d("Location and ${nearbyPayload.size} nearby devices updated successfully")
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

    private fun createChannelAndForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SPORS")
            .setContentText("Location service is active for device safety.")
            .setSmallIcon(R.drawable.ic_location) // ensure ic_location exists in res/drawable
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val req = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            3000L
        ).build()
        fused.requestLocationUpdates(req, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                lastLocation = result.lastLocation
            }
        }, null)
    }

    private fun getTimestampISO8601(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceUniqueId(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            ?: UUID.randomUUID().toString()
    }

    private fun broadcastDevices(devices: ArrayList<ScannedDevice>) {
        val intent = Intent(ACTION_DEVICES_UPDATE)
        intent.putParcelableArrayListExtra(EXTRA_DEVICES, devices)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        scope.cancel()
        scanner.stopScanning()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
