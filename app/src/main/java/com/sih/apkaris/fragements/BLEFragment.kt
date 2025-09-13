package com.sih.apkaris.fragements

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.sih.apkaris.R
import com.sih.apkaris.services.BeaconService
import com.sih.apkaris.services.LocationService

class BLEFragment : Fragment() {

    private lateinit var svLog: ScrollView
    private lateinit var tvLog: TextView
    private lateinit var ivBluetoothToggle: ImageView
    private lateinit var tvBleStatus: TextView

    private var isBleActive = false

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val allGranted = perms.entries.all { it.value == true }
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
                Toast.makeText(requireContext(),
                    "Permissions are required for BLE features. Enable them in settings.",
                    Toast.LENGTH_LONG).show()
            }
        }

        enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter != null && adapter.isEnabled) {
                if (!isBleActive) startBle()
            } else {
                Toast.makeText(requireContext(), "Bluetooth is still disabled", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_ble, container, false)

        ivBluetoothToggle = v.findViewById(R.id.ivBluetoothToggle)
        tvBleStatus = v.findViewById(R.id.tvBleStatus)
        svLog = v.findViewById(R.id.svLog)
        tvLog = v.findViewById(R.id.tvLog)

        ivBluetoothToggle.setOnClickListener {
            onToggleClicked()
        }

        LocationService.jsonLog.observe(viewLifecycleOwner) { json ->
            if (json != null) {
                tvLog.append(json + "\n\n")
                svLog.post { svLog.fullScroll(View.FOCUS_DOWN) }
            }
        }

        LocationService.jsonLog.value?.let {
            if (it.isNotEmpty()) markRunningUI()
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
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        var btScanGranted = true
        var btConnectGranted = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            btScanGranted = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
            btConnectGranted = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        }
        return fineGranted && btScanGranted && btConnectGranted
    }

    private fun requestNeededPermissions() {
        val perms = mutableListOf<String>()
        perms.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.BLUETOOTH_SCAN)
            perms.add(Manifest.permission.BLUETOOTH_CONNECT)
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
        try {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_LONG).show()
                requestNeededPermissions()
                return
            }

            val locIntent = Intent(requireContext(), LocationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(locIntent)
            } else {
                requireContext().startService(locIntent)
            }

            val beaconIntent = Intent(requireContext(), BeaconService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireContext().startForegroundService(beaconIntent)
            } else {
                requireContext().startService(beaconIntent)
            }

            markRunningUI()
            Toast.makeText(requireContext(), "BLE & location services started", Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            t.printStackTrace()
            Toast.makeText(requireContext(), "Failed: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun markRunningUI() {
        isBleActive = true
        ivBluetoothToggle.setImageResource(R.drawable.ic_ble_active)
        ivBluetoothToggle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.blue))
        tvBleStatus.text = "Bluetooth Running"
    }

    private fun stopBle() {
        try {
            val locIntent = Intent(requireContext(), LocationService::class.java)
            requireContext().stopService(locIntent)
            val beaconIntent = Intent(requireContext(), BeaconService::class.java)
            requireContext().stopService(beaconIntent)

            isBleActive = false
            ivBluetoothToggle.setImageResource(R.drawable.ic_ble_offline)
            ivBluetoothToggle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey))
            tvBleStatus.text = "Bluetooth Stopped"
            Toast.makeText(requireContext(), "BLE & location services stopped", Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            t.printStackTrace()
            Toast.makeText(requireContext(), "Failed: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
