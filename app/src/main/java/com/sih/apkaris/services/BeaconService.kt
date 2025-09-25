package com.sih.apkaris.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sih.apkaris.MainActivity
import com.sih.apkaris.R
import java.nio.charset.Charset
import java.util.*

@SuppressLint("MissingPermission") // Permissions are checked in MainActivity before starting
class BeaconService : Service() {

    private val TAG = "BeaconService"

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(TAG, "BLE advertising started successfully.")
        }
        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "BLE advertising onStartFailure: $errorCode")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val deviceId = getDeviceUniqueId()
        val beaconName = "TEAM_SIH_$deviceId"

        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "BeaconChannel")
            .setContentTitle("SPORS Beacon Active")
            .setContentText("Broadcasting as: $beaconName")
            .setSmallIcon(R.drawable.ic_beacon)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        startBeaconing(beaconName)

        return START_STICKY
    }

    private fun startBeaconing(beaconName: String) {
        if (bluetoothAdapter?.isEnabled != true) {
            Log.e(TAG, "Bluetooth is not enabled. Cannot start beacon.")
            stopSelf()
            return
        }

        val advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        if (advertiser == null) {
            Log.e(TAG, "Device does not support BLE advertising.")
            stopSelf()
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()

        val serviceUuid = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")
        val data = AdvertiseData.Builder()
            .addServiceData(serviceUuid, beaconName.toByteArray(Charset.forName("UTF-8")))
            .setIncludeDeviceName(false)
            .build()

        advertiser.startAdvertising(settings, data, advertiseCallback)
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceUniqueId(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_DEVICE"
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        Log.d(TAG, "Beacon service destroyed and advertising stopped.")
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "BeaconChannel",
                "Beacon Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}