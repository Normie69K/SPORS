package com.sih.apkaris.fragements

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.sih.apkaris.GetStartedActivity
import com.sih.apkaris.R

class ProfileFragment : Fragment() {

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
    private lateinit var btnLogout: Button
    private lateinit var ivEditName: ImageView

    private val REQUESTPHONESTATE = 1001

    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

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

        val sharedPref = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val email = sharedPref.getString("loggedInEmail", "Unknown")
        val username = sharedPref.getString("username", email?.substringBefore("@") ?: "Unknown")

        tvUserName.text = username
        tvUserEmail.text = email

        // Edit username
        ivEditName.setOnClickListener {
            val editText = EditText(requireContext())
            editText.setText(tvUserName.text)
            AlertDialog.Builder(requireContext())
                .setTitle("Edit Username")
                .setView(editText)
                .setPositiveButton("Save") { _: DialogInterface, _: Int ->
                    val newName = editText.text.toString().trim()
                    if (newName.isNotEmpty()) {
                        tvUserName.text = newName
                        sharedPref.edit { putString("username", newName) }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
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

        // Phone info (requires permission)
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
                REQUESTPHONESTATE
            )
        }

        btnLogout.setOnClickListener {
            sharedPref.edit { remove("loggedInEmail") }
            val intent = Intent(requireContext(), GetStartedActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    @SuppressLint("HardwareIds", "SetTextI18n")
    private fun showPhoneInfo() {
        val tm = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        // IMEI or Android ID fallback
        val deviceId = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            tm.deviceId ?: "N/A"
        } else {
            Settings.Secure.getString(
                requireContext().contentResolver,
                Settings.Secure.ANDROID_ID
            )
        }
        tvIMEI.text = "Device ID: $deviceId"

        // Phone number
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

        // SIM operator
        val operator = try {
            tm.simOperatorName ?: "Not Available"
        } catch (e: Exception) {
            "Not Available"
        }
        tvSimOperator.text = "SIM Operator: $operator"
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("SetTextI18n")
    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUESTPHONESTATE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showPhoneInfo()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Cannot access IMEI/Phone info: Permission Denied",
                    Toast.LENGTH_LONG
                ).show()
                tvIMEI.text = "Device ID: Permission Denied"
                tvUserPhone.text = "Phone: Permission Denied"
                tvSimOperator.text = "SIM Operator: Permission Denied"
            }
        }
    }
}
