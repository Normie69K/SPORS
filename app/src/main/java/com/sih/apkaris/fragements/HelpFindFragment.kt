package com.sih.apkaris.fragements

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.sih.apkaris.R
import com.sih.apkaris.adapters.ScannedDeviceAdapter
import com.sih.apkaris.bluetooth.ScannedDevice
import com.sih.apkaris.databinding.FragmentHelpFindBinding
import com.sih.apkaris.services.BeaconService
import com.sih.apkaris.services.LocationService

class HelpFindFragment : Fragment() {

    private var _binding: FragmentHelpFindBinding? = null
    private val binding get() = _binding!!

    private var isScanningActive = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private lateinit var scannedDeviceAdapter: ScannedDeviceAdapter

    private val deviceUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // CORRECTED: Use the full path to the constant
            if (intent?.action == LocationService.ACTION_DEVICES_UPDATE) {
                // CORRECTED: Use the full path to the constant
                val devices = intent.getParcelableArrayListExtra<ScannedDevice>(LocationService.EXTRA_DEVICES)
                devices?.let {
                    scannedDeviceAdapter.updateDevices(it)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = requireContext().getSharedPreferences("appState", Context.MODE_PRIVATE)
        isScanningActive = sharedPref.getBoolean("isScanningActive", false)

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            if (perms.entries.all { it.value }) {
                startScanning()
            } else {
                Toast.makeText(requireContext(), "All permissions are required to help find devices.", Toast.LENGTH_LONG).show()
            }
        }

        enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (BluetoothAdapter.getDefaultAdapter()?.isEnabled == true) {
                startScanning()
            } else {
                Toast.makeText(requireContext(), "Bluetooth must be enabled to help find devices.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHelpFindBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        updateUI()

        binding.buttonStartScanning.setOnClickListener {
            if (isScanningActive) {
                stopScanning()
            } else {
                startScanning()
            }
        }

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        // CORRECTED: Use the full path to the constant
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            deviceUpdateReceiver,
            IntentFilter(LocationService.ACTION_DEVICES_UPDATE)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(deviceUpdateReceiver)
    }

    private fun setupRecyclerView() {
        scannedDeviceAdapter = ScannedDeviceAdapter(emptyList())
        binding.recyclerViewScannedDevices.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = scannedDeviceAdapter
        }
    }

    private fun startScanning() {
        if (!hasRequiredPermissions()) {
            requestRequiredPermissions()
            return
        }

        if (BluetoothAdapter.getDefaultAdapter()?.isEnabled != true) {
            requestEnableBluetooth()
            return
        }

        val locIntent = Intent(requireContext(), LocationService::class.java)
        ContextCompat.startForegroundService(requireContext(), locIntent)

        val beaconIntent = Intent(requireContext(), BeaconService::class.java)
        ContextCompat.startForegroundService(requireContext(), beaconIntent)

        isScanningActive = true
        requireContext().getSharedPreferences("appState", Context.MODE_PRIVATE).edit().putBoolean("isScanningActive", true).apply()
        updateUI()
        Toast.makeText(requireContext(), "Scanning started. Thank you for helping!", Toast.LENGTH_SHORT).show()
    }

    private fun stopScanning() {
        requireContext().stopService(Intent(requireContext(), LocationService::class.java))
        requireContext().stopService(Intent(requireContext(), BeaconService::class.java))

        isScanningActive = false
        requireContext().getSharedPreferences("appState", Context.MODE_PRIVATE).edit().putBoolean("isScanningActive", false).apply()
        updateUI()
        scannedDeviceAdapter.updateDevices(emptyList())
        Toast.makeText(requireContext(), "Scanning stopped.", Toast.LENGTH_SHORT).show()
    }

    private fun updateUI() {
        if (isScanningActive) {
            binding.buttonStartScanning.text = "Stop Scanning"
            binding.textViewStatus.text = "Your device is actively scanning"
            binding.textViewStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_wifi, 0, 0, 0)
        } else {
            binding.buttonStartScanning.text = "Start Scanning"
            binding.textViewStatus.text = "Your device is not currently scanning"
            binding.textViewStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_wifi_off, 0, 0, 0)
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val btScan = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            val btConnect = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            val btAdvertise = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
            return fineLocation && btScan && btConnect && btAdvertise
        }
        return fineLocation
    }

    private fun requestRequiredPermissions() {
        val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.BLUETOOTH_SCAN)
            perms.add(Manifest.permission.BLUETOOTH_CONNECT)
            perms.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }
        permissionLauncher.launch(perms.toTypedArray())
    }

    private fun requestEnableBluetooth() {
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothLauncher.launch(enableIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}