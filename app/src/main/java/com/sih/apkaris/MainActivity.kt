package com.sih.apkaris

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.sih.apkaris.fragements.*
import me.ibrahimsn.lib.SmoothBottomBar

class MainActivity : AppCompatActivity() {

    private lateinit var bottomBar: SmoothBottomBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomBar = findViewById(R.id.bottomBar)

        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val loggedInEmail = sharedPref.getString("loggedInEmail", null)

        if (loggedInEmail != null) {
            // User already logged in → show Home and bottom bar
            showHomeFragment()
            showBottomBar()
        } else {
            // Not logged in → show login/register without bottom bar
            if (savedInstanceState == null) {
                showLoginFragment()
            }

            // Prepopulate test accounts
            if (!sharedPref.contains("test1_email")) {
                sharedPref.edit().apply {
                    putString("test1_email", "test1@gmail.com")
                    putString("test1_password", "password1")
                    putString("test2_email", "test2@gmail.com")
                    putString("test2_password", "password2")
                    apply()
                }
            }
        }

        // Handle bottom bar selections
        bottomBar.onItemSelected = { pos ->
            when (pos) {
                0 -> showProfileFragment()
                1 -> showHomeFragment()
                2 -> showBleFragment()
            }
        }
    }

    fun showLoginFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, LoginFragment())
        }
    }

    fun showRegisterFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, RegisterFragment())
        }
    }

    fun showHomeFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, HomeFragment())
        }
    }

    fun showProfileFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, ProfileFragment())
        }
    }

    fun showBleFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, BLEFragment())
        }
    }

    fun goToHome(email: String) {
        // Save login info
        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("loggedInEmail", email).apply()

        showHomeFragment()
        showBottomBar()
    }

    private fun showBottomBar() {
        bottomBar.visibility = View.VISIBLE
    }
}
