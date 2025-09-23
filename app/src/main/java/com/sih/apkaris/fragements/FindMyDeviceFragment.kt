package com.sih.apkaris.fragements

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sih.apkaris.R
import com.sih.apkaris.databinding.FragmentFindMyDeviceBinding
import com.sih.apkaris.network.Device
import com.sih.apkaris.network.RetrofitClient
import com.sih.apkaris.network.TrackPoint
import kotlinx.coroutines.launch

class FindMyDeviceFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentFindMyDeviceBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap
    private var userDevices: List<Device> = emptyList()
    private val TAG = "FindMyDeviceFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFindMyDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        mapFragment.getMapAsync(this)

        loadDevicesFromPrefs()
        setupDeviceDropdown()

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        val defaultLocation = LatLng(20.5937, 78.9629)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 4f))
    }

    private fun loadDevicesFromPrefs() {
        val sharedPref = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val devicesJson = sharedPref.getString("devices", null)
        if (devicesJson != null) {
            try {
                val type = object : TypeToken<List<Device>>() {}.type
                userDevices = Gson().fromJson(devicesJson, type)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse devices from SharedPreferences", e)
                Toast.makeText(context, "Could not load device list.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDeviceDropdown() {
        if (userDevices.isEmpty()) {
            binding.dropdownLayoutDevice.hint = "No devices found"
            return
        }
        val deviceNames = userDevices.map { "${it.devicename} (${it.deviceid})" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, deviceNames)
        binding.autocompleteTextViewDevice.setAdapter(adapter)
        binding.autocompleteTextViewDevice.setOnItemClickListener { _, _, position, _ ->
            val selectedDevice = userDevices[position]
            fetchDeviceLocationFromServer(selectedDevice)
        }
    }

    private fun fetchDeviceLocationFromServer(device: Device) {
        Toast.makeText(context, "Fetching location history for ${device.devicename}...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getDeviceLocationHistory(device.deviceid)
                if (response.isSuccessful && response.body()?.success == true) {
                    val locationHistory = response.body()?.data
                    if (!locationHistory.isNullOrEmpty()) {
                        updateMapWithHistory(locationHistory, device)
                    } else {
                        Toast.makeText(context, "${device.devicename} has no location history.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Could not find device history."
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "API call failed with exception", e)
                Toast.makeText(context, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateMapWithHistory(history: List<TrackPoint>, device: Device) {
        googleMap.clear()
        val boundsBuilder = LatLngBounds.Builder()

        history.forEach { point ->
            val lat = point.latitude.toDoubleOrNull()
            val lng = point.longitude.toDoubleOrNull()
            if (lat != null && lng != null) {
                val location = LatLng(lat, lng)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title(device.devicename)
                        .snippet("Seen at: ${point.timestamp}")
                )
                boundsBuilder.include(location)
            }
        }

        try {
            val bounds = boundsBuilder.build()
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } catch (e: IllegalStateException) {
            history.lastOrNull()?.let { lastPoint ->
                val lat = lastPoint.latitude.toDoubleOrNull()
                val lng = lastPoint.longitude.toDoubleOrNull()
                if (lat != null && lng != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15f))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}