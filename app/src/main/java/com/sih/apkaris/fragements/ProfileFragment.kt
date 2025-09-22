package com.sih.apkaris.fragements

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.sih.apkaris.MainActivity
import com.sih.apkaris.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences
    private val REQUEST_PHONE_STATE = 1001

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        sharedPref = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserInfo()
        displayDeviceInfo()
        checkPhonePermissions()

        binding.ivEditName.setOnClickListener {
            showEditDialog("Edit Username", binding.tvUserName.text.toString()) { newName ->
                binding.tvUserName.text = newName
                sharedPref.edit { putString("username", newName) }
            }
        }

        binding.btnLogout.setOnClickListener {
            (activity as? MainActivity)?.logout()
        }
    }

    private fun loadUserInfo() {
        val email = sharedPref.getString("username", "user@email.com") // username from API is email
        val name = sharedPref.getString("displayName", email?.substringBefore("@") ?: "Unknown User")

        binding.tvUserName.text = name
        binding.tvUserEmail.text = email
    }

    private fun displayDeviceInfo() {
        binding.tvDeviceModel.text = "Model: ${Build.MODEL}"
        binding.tvDeviceManufacturer.text = "Manufacturer: ${Build.MANUFACTURER}"
        binding.tvAndroidVersion.text = "Android Version: ${Build.VERSION.RELEASE}"

        val bm = requireContext().getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        binding.tvBatteryLevel.text = "Battery: $batteryLevel%"

        val stat = StatFs(Environment.getDataDirectory().path)
        val freeGB = stat.availableBytes / (1024.0 * 1024.0 * 1024.0)
        val totalGB = stat.totalBytes / (1024.0 * 1024.0 * 1024.0)
        binding.tvStorageInfo.text = String.format("Storage: %.1f GB free / %.1f GB", freeGB, totalGB)
    }

    private fun checkPhonePermissions() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            showPhoneInfo()
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), REQUEST_PHONE_STATE)
        }
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun showPhoneInfo() {
        val tm = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val deviceId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)

        binding.tvIMEI.text = "Device ID: $deviceId"
        binding.tvUserPhone.text = "Phone: ${tm.line1Number ?: "Not Available"}"
        binding.tvSimOperator.text = "SIM Operator: ${tm.simOperatorName ?: "Not Available"}"
    }

    private fun showEditDialog(title: String, currentValue: String, onSave: (String) -> Unit) {
        val input = EditText(requireContext()).apply { setText(currentValue) }
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setPositiveButton("Save") { _, _ -> onSave(input.text.toString().trim()) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PHONE_STATE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showPhoneInfo()
        } else {
            Toast.makeText(requireContext(), "Phone state permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}