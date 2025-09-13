package com.sih.apkaris.fragements

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
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
import android.widget.*
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.sih.apkaris.GetStartedActivity
import com.sih.apkaris.R

class ProfileFragment : Fragment() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvDeviceModel: TextView
    private lateinit var tvDeviceManufacturer: TextView
    private lateinit var tvAndroidVersion: TextView
    private lateinit var tvIMEI: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvSimOperator: TextView
    private lateinit var tvBatteryLevel: TextView
    private lateinit var tvStorageInfo: TextView
    private lateinit var deviceIdText: TextView
    private lateinit var btnLogout: Button
    private lateinit var ivEditName: ImageView
    private lateinit var ivEditDevice: ImageView

    private val REQUEST_PHONE_STATE = 1001
    private var currentDeviceId: String = "Unknown"

    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        sharedPref = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)

        // UI references
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        tvDeviceModel = view.findViewById(R.id.tvDeviceModel)
        tvDeviceManufacturer = view.findViewById(R.id.tvDeviceManufacturer)
        tvAndroidVersion = view.findViewById(R.id.tvAndroidVersion)
        tvIMEI = view.findViewById(R.id.tvIMEI)
        tvUserPhone = view.findViewById(R.id.tvUserPhone)
        tvSimOperator = view.findViewById(R.id.tvSimOperator)
        tvBatteryLevel = view.findViewById(R.id.tvBatteryLevel)
        tvStorageInfo = view.findViewById(R.id.tvStorageInfo)
        btnLogout = view.findViewById(R.id.btnLogout)
        ivEditName = view.findViewById(R.id.ivEditName)
        ivEditDevice = view.findViewById(R.id.EdtDevice)
        deviceIdText = view.findViewById(R.id.deviceId)

        // Load stored values
        val email = sharedPref.getString("loggedInEmail", "user@email.com")
        val username = sharedPref.getString("username", email?.substringBefore("@") ?: "Unknown User")
        val storedDeviceId = sharedPref.getString("deviceId", "Device-01")

        tvUserName.text = username
        tvUserEmail.text = email
        tvUserPhone.text = sharedPref.getString("phone", "+91 XXXXXXX")
        deviceIdText.text = storedDeviceId
        currentDeviceId = storedDeviceId ?: username ?: "Unknown"

        // Edit username
        ivEditName.setOnClickListener {
            showEditDialog("Edit Username", tvUserName.text.toString()) { newName ->
                tvUserName.text = newName
                sharedPref.edit { putString("username", newName) }
                currentDeviceId = newName
                propagateDeviceIdChange(newName)
            }
        }

        // Edit Device ID
        ivEditDevice.setOnClickListener {
            showEditDialog("Edit Device ID", deviceIdText.text.toString()) { newId ->
                deviceIdText.text = newId
                sharedPref.edit().putString("deviceId", newId).apply()
                currentDeviceId = newId
                propagateDeviceIdChange(newId)
            }
        }

        // Device info
        tvDeviceModel.text = "Model: ${Build.MODEL}"
        tvDeviceManufacturer.text = "Manufacturer: ${Build.MANUFACTURER}"
        tvAndroidVersion.text = "Android Version: ${Build.VERSION.RELEASE}"

        // Battery info
        val bm = requireContext().getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        tvBatteryLevel.text = "Battery: $batteryLevel%"

        // Storage info
        val stat = StatFs(Environment.getDataDirectory().path)
        val freeGB = stat.availableBytes / (1024.0 * 1024.0 * 1024.0)
        val totalGB = stat.totalBytes / (1024.0 * 1024.0 * 1024.0)
        tvStorageInfo.text = String.format("Storage: %.1f GB free / %.1f GB", freeGB, totalGB)

        // Phone info
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            showPhoneInfo()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.READ_SMS
                ),
                REQUEST_PHONE_STATE
            )
        }

        // Logout
        btnLogout.setOnClickListener {
            sharedPref.edit().clear().apply()
            val intent = Intent(requireContext(), GetStartedActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    private fun showEditDialog(title: String, currentValue: String, onSave: (String) -> Unit) {
        val input = EditText(requireContext()).apply {
            setText(currentValue)
            inputType = InputType.TYPE_CLASS_TEXT
        }
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newVal = input.text.toString().trim()
                if (newVal.isNotEmpty()) {
                    onSave(newVal)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun propagateDeviceIdChange(newId: String) {
        val bleFragment = parentFragmentManager.findFragmentByTag("BLEFragment") as? BLEFragment
        bleFragment?.updateDeviceId(newId)
    }

    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    @SuppressLint("HardwareIds")
    private fun showPhoneInfo() {
        val tm = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val deviceId = try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                tm.deviceId ?: "N/A"
            } else {
                Settings.Secure.getString(
                    requireContext().contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            }
        } catch (e: Exception) {
            "N/A"
        }
        tvIMEI.text = "Device ID: $deviceId"

        val phone = try {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_SMS
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_PHONE_NUMBERS
                ) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                "Permission Required"
            } else {
                tm.line1Number ?: "Not Available"
            }
        } catch (e: Exception) {
            "Not Available"
        }
        tvUserPhone.text = "Phone: $phone"

        val operator = try {
            tm.simOperatorName ?: "Not Available"
        } catch (e: Exception) {
            "Not Available"
        }
        tvSimOperator.text = "SIM Operator: $operator"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PHONE_STATE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showPhoneInfo()
            } else {
                tvIMEI.text = "Device ID: Permission Denied"
                tvUserPhone.text = "Phone: Permission Denied"
                tvSimOperator.text = "SIM Operator: Permission Denied"
                Toast.makeText(
                    requireContext(),
                    "Cannot access phone info: Permission Denied",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
