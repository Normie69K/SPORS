package com.sih.apkaris.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Parcelable
import com.sih.apkaris.utils.Logger
import kotlinx.parcelize.Parcelize
import java.util.concurrent.ConcurrentHashMap

@Parcelize
data class ScannedDevice(val address: String, val name: String?, val rssi: Int) : Parcelable

class BluetoothScanner(private val ctx: Context) {

    private var scanner: BluetoothLeScanner? = null
    private var callback: ScanCallback? = null

    private val devices = ConcurrentHashMap<String, ScannedDevice>()

    @SuppressLint("MissingPermission")
    fun startScanning() {
        try {
            val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
            if (adapter == null || !adapter.isEnabled) {
                Logger.e("BluetoothAdapter null or disabled")
                return
            }
            scanner = adapter.bluetoothLeScanner
            if (scanner == null) {
                Logger.e("BluetoothLeScanner null")
                return
            }

            callback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    val device = result.device ?: return
                    val address = device.address ?: return
                    devices[address] = ScannedDevice(address, device.name, result.rssi)
                }

                override fun onScanFailed(errorCode: Int) {
                    Logger.e("Scan failed code=$errorCode")
                }
            }

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            scanner?.startScan(null, settings, callback)
            Logger.d("BLE scan started")
        } catch (t: Throwable) {
            Logger.e("startScanning error", t)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        try {
            scanner?.stopScan(callback)
            callback = null
            scanner = null
            devices.clear()
            Logger.d("BLE scan stopped")
        } catch (t: Throwable) {
            Logger.e("stopScanning error", t)
        }
    }

    fun snapshotNearby(): List<ScannedDevice> {
        return devices.values.toList()
    }
}