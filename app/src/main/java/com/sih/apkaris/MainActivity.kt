package com.sih.apkaris

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.sih.apkaris.fragements.*
import com.sih.apkaris.fragments.HomeFragment
import com.sih.apkaris.services.BeaconService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPref.getString("token", null)

        if (sessionToken != null) {
            showHomeUI()
        } else {
            if (savedInstanceState == null) {
                showLoginFragment()
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            // Permissions are granted, the service can run correctly.
        } else {
            Toast.makeText(this, "Bluetooth permissions are required for the beacon feature.", Toast.LENGTH_LONG).show()
        }
    }


    fun navigateTo(fragment: Fragment, addToBackStack: Boolean = true) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, fragment)
            if (addToBackStack) {
                addToBackStack(fragment::class.java.simpleName)
            }
        }
    }

    fun showLoginFragment() {
        navigateTo(LoginFragment(), addToBackStack = false)
    }

    fun showSignUpFragment(addToBackStack: Boolean = true) {
        navigateTo(SignUpFragment(), addToBackStack)
    }

    fun showHomeFragment(addToBackStack: Boolean = true) {
        navigateTo(HomeFragment(), addToBackStack)
    }

    fun showReportLostFragment() = navigateTo(ReportLostFragment())
    fun showFindMyDeviceFragment() = navigateTo(FindMyDeviceFragment())
    fun showHelpFindFragment() = navigateTo(HelpFindFragment())
    fun showReportFoundFragment() = navigateTo(ReportFoundFragment())
    fun showProfileFragment() = navigateTo(ProfileFragment())
    fun showForgotPasswordFragment() = navigateTo(ForgotPasswordFragment())
    fun showVerificationFragment() = navigateTo(VerificationFragment())
    fun showCreateNewPasswordFragment() = navigateTo(CreateNewPasswordFragment())

    fun showHomeUI() {
        showHomeFragment(addToBackStack = false)
        checkAndRequestBeaconPermissions()
        showHomeFragment(addToBackStack = false)
    }



    fun logout() {
        stopService(Intent(this, BeaconService::class.java))
        getSharedPreferences("userPrefs", Context.MODE_PRIVATE).edit { clear() }
        showLoginFragment()

    }
    private fun checkAndRequestBeaconPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasConnectPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            val hasAdvertisePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED

            if (!hasConnectPermission || !hasAdvertisePermission) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_ADVERTISE
                    )
                )
            }
        }
        // For older Android versions, permissions are granted at install time.
    }

    // ... (All other navigation functions remain the same)

}