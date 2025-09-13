package com.sih.apkaris.services

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.sih.apkaris.R
import com.sih.apkaris.utils.Logger
import java.nio.charset.Charset
import java.util.*

class BeaconService : Service() {

    private val adapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val advertiser by lazy { adapter?.bluetoothLeAdvertiser }
    private var callback: AdvertiseCallback? = null
    private val channelId = "BeaconChannel_v1"

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        startAdvertisingSafe()
    }

    private fun startForegroundServiceNotification() {
        val notif = NotificationCompat.Builder(this, LocationService.CHANNEL_ID)
            .setContentTitle("APkARiS — Beacon")
            .setContentText("Broadcasting device presence")
            .setSmallIcon(R.drawable.ic_beacon)
            .build()
        startForeground(1002, notif)
    }

    private fun startAdvertisingSafe() {
        try {
            if (adapter == null || advertiser == null) {
                Logger.e("No BT adapter or advertiser")
                return
            }

            val deviceId = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: UUID.randomUUID().toString()
            val bytes = deviceId.toByteArray(Charset.forName("UTF-8"))

            val data = AdvertiseData.Builder()
                .addServiceData(ParcelUuid(UUID.fromString("0000feed-0000-1000-8000-00805f9b34fb")), bytes)
                .setIncludeDeviceName(false)
                .build()

            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(false)
                .build()

            callback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    Logger.d("Advertise started")
                }
                override fun onStartFailure(errorCode: Int) {
                    Logger.e("Advertise failed code=$errorCode")
                }
            }

            advertiser?.startAdvertising(settings, data, callback)
        } catch (t: Throwable) {
            Logger.e("startAdvertisingSafe error", t)
        }
    }

    override fun onDestroy() {
        try {
            callback?.let { advertiser?.stopAdvertising(it) }
        } catch (t: Throwable) { /* ignore */ }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
