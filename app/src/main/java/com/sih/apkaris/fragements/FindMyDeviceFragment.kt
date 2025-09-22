package com.sih.apkaris.fragements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sih.apkaris.R
import com.sih.apkaris.databinding.FragmentFindMyDeviceBinding

class FindMyDeviceFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentFindMyDeviceBinding? = null
    private val binding get() = _binding!!

    private lateinit var googleMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFindMyDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.buttonFindDevice.setOnClickListener {
            searchForDevice()
        }

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        val defaultLocation = LatLng(20.5937, 78.9629) // India
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 4f))
    }

    private fun searchForDevice() {
        val deviceId = binding.editTextDeviceId.text.toString()
        if (deviceId.isBlank()) {
            Toast.makeText(context, "Please enter a Device ID", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Call your API with the deviceId to get its LatLng coordinates
        Toast.makeText(context, "Searching for device: $deviceId", Toast.LENGTH_SHORT).show()

        // Dummy location for demonstration
        val deviceLocation = LatLng(19.0760, 72.8777) // Mumbai

        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(deviceLocation).title("Device: $deviceId"))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(deviceLocation, 15f))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}