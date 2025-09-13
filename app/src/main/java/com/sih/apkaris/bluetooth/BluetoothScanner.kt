package com.sih.apkaris.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import com.sih.apkaris.utils.Logger
import java.util.concurrent.ConcurrentHashMap

class BluetoothScanner(private val ctx: Context) {

    private var scanner: BluetoothLeScanner? = null
    private var callback: ScanCallback? = null

    // Map of address -> rssi (keeps most recent rssi)
    private val devices = ConcurrentHashMap<String, Int>()

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
                    val d = result.device ?: return
                    val address = d.address ?: return
                    devices[address] = result.rssi
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    results?.forEach { r ->
                        val d = r.device ?: return@forEach
                        val address = d.address ?: return@forEach
                        devices[address] = r.rssi
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    Logger.e("Scan failed code=$errorCode")
                }
            }

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                scanner?.startScan(null, settings, callback)
            } else {
                scanner?.startScan(callback)
            }
            Logger.d("BLE scan started")
        } catch (t: Throwable) {
            Logger.e("startScanning error", t)
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        try {
            scanner?.let { s ->
                callback?.let { s.stopScan(it) }
            }
            callback = null
            scanner = null
            devices.clear()
            Logger.d("BLE scan stopped")
        } catch (t: Throwable) {
            Logger.e("stopScanning error", t)
        }
    }

    // snapshot of current nearby devices (and clear older ones if you want)
    fun snapshotNearby(): List<Pair<String, Int>> {
        return devices.entries.map { it.key to it.value }
    }

    fun clearSnapshot() {
        devices.clear()
    }
}
