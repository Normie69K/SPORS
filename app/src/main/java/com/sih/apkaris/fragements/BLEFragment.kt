package com.sih.apkaris.fragements

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.sih.apkaris.R
import com.sih.apkaris.services.BeaconService
import com.sih.apkaris.services.LocationService
import java.nio.charset.StandardCharsets
import java.util.*

class BLEFragment : Fragment() {

    private lateinit var svLog: ScrollView
    private lateinit var tvLog: TextView
    private lateinit var ivBluetoothToggle: ImageView
    private lateinit var tvBleStatus: TextView

    private var isBleActive = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>

    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var advertiseCallback: AdvertiseCallback? = null
    private var userDeviceId: String? = null
    private lateinit var sharedPref: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        isBleActive = sharedPref.getBoolean("bleActive", false)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
                val allGranted = perms.entries.all { it.value }
                if (allGranted) {
                    val adapter = BluetoothAdapter.getDefaultAdapter()
                    if (adapter == null) {
                        Toast.makeText(requireContext(), "Bluetooth not supported", Toast.LENGTH_SHORT).show()
                    } else if (!adapter.isEnabled) {
                        requestEnableBluetooth()
                    } else {
                        if (isBleActive) stopBle() else startBle()
                    }
                } else {
                    Toast.makeText(requireContext(), "Permissions required for BLE features", Toast.LENGTH_LONG).show()
                }
            }

        enableBluetoothLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val adapter = BluetoothAdapter.getDefaultAdapter()
                if (adapter != null && adapter.isEnabled) {
                    if (!isBleActive) startBle()
                } else {
                    Toast.makeText(requireContext(), "Bluetooth still disabled", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_ble, container, false)

        ivBluetoothToggle = v.findViewById(R.id.ivBluetoothToggle)
        tvBleStatus = v.findViewById(R.id.tvBleStatus)
        svLog = v.findViewById(R.id.svLog)
        tvLog = v.findViewById(R.id.tvLog)

        ivBluetoothToggle.setOnClickListener { onToggleClicked() }

        userDeviceId = sharedPref.getString("deviceId", "Device-${UUID.randomUUID().toString().take(6)}")

        LocationService.jsonLog.observe(viewLifecycleOwner) { json ->
            if (json != null) {
                tvLog.append(json + "\n\n")
                svLog.post { svLog.fullScroll(View.FOCUS_DOWN) }
            }
        }

        LocationService.jsonLog.value?.let {
            if (it.isNotEmpty() && isBleActive) markRunningUI()
        }

        if (isBleActive) {
            startBle()
        }

        return v
    }

    private fun onToggleClicked() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Toast.makeText(requireContext(), "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            return
        }
        if (!areAllNeededPermissionsGranted()) {
            requestNeededPermissions()
            return
        }
        if (!adapter.isEnabled) {
            requestEnableBluetooth()
            return
        }
        if (isBleActive) stopBle() else startBle()
    }

    private fun areAllNeededPermissionsGranted(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        var btScanGranted = true
        var btConnectGranted = true
        var btAdvertiseGranted = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            btScanGranted =
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            btConnectGranted =
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            btAdvertiseGranted =
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
        }
        return fineGranted && btScanGranted && btConnectGranted && btAdvertiseGranted
    }

    private fun requestNeededPermissions() {
        val perms = mutableListOf<String>()
        perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.BLUETOOTH_SCAN)
            perms.add(Manifest.permission.BLUETOOTH_CONNECT)
            perms.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        permissionLauncher.launch(perms.toTypedArray())
    }

    private fun requestEnableBluetooth() {
        try {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableIntent)
        } catch (t: Exception) {
            val settings = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            enableBluetoothLauncher.launch(settings)
        }
    }

    private fun startBle() {
        if (!areAllNeededPermissionsGranted()) {
            Toast.makeText(requireContext(), "All permissions required", Toast.LENGTH_LONG).show()
            requestNeededPermissions()
            return
        }

        val locIntent = Intent(requireContext(), LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) requireContext().startForegroundService(locIntent)
        else requireContext().startService(locIntent)

        val beaconIntent = Intent(requireContext(), BeaconService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) requireContext().startForegroundService(beaconIntent)
        else requireContext().startService(beaconIntent)

        startAdvertising()

        isBleActive = true
        sharedPref.edit().putBoolean("bleActive", true).apply()

        markRunningUI()
        Toast.makeText(requireContext(), "BLE started with ID: $userDeviceId", Toast.LENGTH_SHORT).show()
    }

    private fun stopBle() {
        val locIntent = Intent(requireContext(), LocationService::class.java)
        requireContext().stopService(locIntent)
        val beaconIntent = Intent(requireContext(), BeaconService::class.java)
        requireContext().stopService(beaconIntent)

        stopAdvertising()

        isBleActive = false
        sharedPref.edit().putBoolean("bleActive", false).apply()

        ivBluetoothToggle.setImageResource(R.drawable.ic_ble_offline)
        ivBluetoothToggle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey))
        tvBleStatus.text = "Bluetooth Stopped"
        Toast.makeText(requireContext(), "BLE stopped", Toast.LENGTH_SHORT).show()
    }

    private fun markRunningUI() {
        ivBluetoothToggle.setImageResource(R.drawable.ic_ble_active)
        ivBluetoothToggle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.blue))
        tvBleStatus.text = "Bluetooth Running with ID: $userDeviceId"
    }

    private fun startAdvertising() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothLeAdvertiser = adapter.bluetoothLeAdvertiser ?: return

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()

        val payload = "{\"userId\":\"$userDeviceId\"}"
        val data = AdvertiseData.Builder()
            .addServiceData(
                ParcelUuid(UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")),
                payload.toByteArray(StandardCharsets.UTF_8)
            )
            .setIncludeDeviceName(false)
            .build()

        advertiseCallback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.d("BLE", "Advertising started")
            }

            override fun onStartFailure(errorCode: Int) {
                Log.e("BLE", "Advertising failed: $errorCode")
                Toast.makeText(requireContext(), "Advertising failed: $errorCode", Toast.LENGTH_LONG).show()
            }
        }

        bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    private fun stopAdvertising() {
        advertiseCallback?.let { bluetoothLeAdvertiser?.stopAdvertising(it) }
        advertiseCallback = null
    }

    fun updateDeviceId(newId: String) {
        userDeviceId = newId
        sharedPref.edit().putString("deviceId", newId).apply()
        if (isBleActive) {
            stopAdvertising()
            startAdvertising()
            markRunningUI()
        }
    }
}
