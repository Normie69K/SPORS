package com.sih.apkaris

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.commit
import com.sih.apkaris.fragements.*
import com.sih.apkaris.fragments.HomeFragment
import me.ibrahimsn.lib.SmoothBottomBar

class MainActivity : AppCompatActivity() {

    private lateinit var bottomBar: SmoothBottomBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomBar = findViewById(R.id.bottomBar)

        // On app start, check if a session token is already saved
        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val sessionToken = sharedPref.getString("token", null)

        if (sessionToken != null) {
            // Token found, user is already logged in
            showHomeUI()
        } else {
            // No token, user needs to log in
            if (savedInstanceState == null) { // Prevents re-adding fragment on rotation
                showLoginFragment()
            }
        }

        // Handle navigation clicks on the bottom bar
        bottomBar.onItemSelected = { position ->
            when (position) {
                0 -> showProfileFragment()
                1 -> showHomeFragment()
                2 -> showBleFragment()
                // Add more cases if you have more tabs
            }
        }
    }

    // --- Navigation Functions ---

    fun showLoginFragment() {
        hideBottomBar() // Ensure bottom bar is hidden on the login screen
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, LoginFragment())
        }
    }

    fun showRegisterFragment() {
        hideBottomBar() // Ensure bottom bar is hidden on the register screen
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, RegisterFragment())
        }
    }

    private fun showHomeFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, HomeFragment())
        }
    }

    private fun showProfileFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, ProfileFragment())
        }
    }

    private fun showBleFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, BLEFragment())
        }
    }

    // --- UI State Management ---

    /**
     * This is called from LoginFragment after a successful API login.
     * It transitions the app to the main "logged-in" state.
     */
    fun showHomeUI() {
        showBottomBar()
        bottomBar.itemActiveIndex = 1 // Set Home as the selected tab
        showHomeFragment()
    }

    /**
     * Call this function from a fragment (e.g., ProfileFragment) to log the user out.
     */
    fun logout() {
        // Clear all saved data from SharedPreferences
        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        sharedPref.edit {
            clear()
        }

        // Go back to the login screen
        showLoginFragment()
    }

    private fun showBottomBar() {
        bottomBar.visibility = View.VISIBLE
    }

    private fun hideBottomBar() {
        bottomBar.visibility = View.GONE
    }
}