package com.sih.apkaris.fragments

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sih.apkaris.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.animation.ObjectAnimator
import androidx.annotation.RequiresPermission
import com.sih.apkaris.utils.NetworkUtils
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserStatus: TextView
    private lateinit var layoutLoginHistory: LinearLayout
    private lateinit var fabRefresh: FloatingActionButton
    private lateinit var btnReportLost: Button
    private lateinit var btnTrackDevice: Button
    private lateinit var mMap: GoogleMap
    private var mapReady = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val serverUrl = "https://phone-lost-and-found.vercel.app/storeLocation"

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) @androidx.annotation.RequiresPermission(
            allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]
        ) { granted ->
            if (granted) {
                obtainLocationAndShow()
            } else {
                Toast.makeText(requireContext(), "Location permission required to show map", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserStatus = view.findViewById(R.id.tvUserStatus)
        layoutLoginHistory = view.findViewById(R.id.layoutLoginHistory)
        fabRefresh = view.findViewById(R.id.fabRefresh)
        btnReportLost = view.findViewById(R.id.btnReportLost)
        btnTrackDevice = view.findViewById(R.id.btnTrackDevice)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        loadUserInfo()
        populateLoginHistory()

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fabRefresh.setOnClickListener { animateRefreshAndReload() }
        btnReportLost.setOnClickListener { showReportDialog(isTrack = false) }
        btnTrackDevice.setOnClickListener { showReportDialog(isTrack = true) }

        return view
    }

    private fun loadUserInfo() {
        val sharedPref = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val email = sharedPref.getString("loggedInEmail", "Unknown")
        val name = if (email != null) sharedPref.getString("${email}_name", email.substringBefore("@")) else "Unknown"
        tvUserName.text = name
        tvUserStatus.text = "Active Now"
    }

    private fun populateLoginHistory() {
        layoutLoginHistory.removeAllViews()
        val demo = listOf(
            "Pixel 7 Pro • ${formatNowMinusDays(0, "HH:mm, dd MMM")}",
            "Samsung S21 • ${formatNowMinusDays(1, "HH:mm, dd MMM")}"
        )
        for (s in demo) {
            val tv = TextView(requireContext())
            tv.text = s
            tv.setPadding(6, 8, 6, 8)
            layoutLoginHistory.addView(tv)
        }
    }

    private fun formatNowMinusDays(days: Int, fmt: String): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return SimpleDateFormat(fmt, Locale.getDefault()).format(cal.time)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun animateRefreshAndReload() {
        val animator = ObjectAnimator.ofFloat(fabRefresh, "rotation", 0f, 360f)
        animator.duration = 600
        animator.interpolator = LinearInterpolator()
        animator.start()
        refreshMapAndHistory()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun refreshMapAndHistory() {
        populateLoginHistory()
        if (mapReady) obtainLocationAndShow()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapReady = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = false
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtainLocationAndShow()
        } else {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun obtainLocationAndShow() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) showLocationOnMap(loc)
                else addMarkerAndMove(LatLng(19.0760, 72.8777), "Default (Mumbai)")
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Location failed: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        } catch (t: Throwable) {
            Toast.makeText(requireContext(), "Location error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLocationOnMap(loc: Location) {
        val pos = LatLng(loc.latitude, loc.longitude)
        addMarkerAndMove(pos, "You are here")
    }

    private fun addMarkerAndMove(pos: LatLng, title: String) {
        if (::mMap.isInitialized) {
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(pos).title(title))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 14f))
        }
    }

    private fun showReportDialog(isTrack: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(if (isTrack) "Track Device" else "Report Lost")

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_report_track, null, false)
        val etDeviceId = view.findViewById<EditText>(R.id.etDeviceId)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etNote = view.findViewById<EditText>(R.id.etNote)

        builder.setView(view)
        builder.setPositiveButton("Submit") { _: DialogInterface, _: Int ->
            val deviceId = etDeviceId.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val note = etNote.text.toString().trim()
            if (deviceId.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Device ID and phone required", Toast.LENGTH_LONG).show()
                return@setPositiveButton
            }
            val json = JSONObject().apply {
                put("action", if (isTrack) "track" else "report_lost")
                put("deviceId", deviceId)
                put("phone", phone)
                put("note", note)
                put("submittedAt", System.currentTimeMillis())
            }.toString()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    NetworkUtils.sendDataToServer(json, serverUrl, object : okhttp3.Callback {
                        override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(requireContext(), "Network error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                            CoroutineScope(Dispatchers.Main).launch {
                                if (response.isSuccessful)
                                    Toast.makeText(requireContext(), "Submitted successfully", Toast.LENGTH_LONG).show()
                                else
                                    Toast.makeText(requireContext(), "Submission failed: ${response.code}", Toast.LENGTH_LONG).show()
                            }
                        }
                    })
                } catch (t: Throwable) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(requireContext(), "Error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}
