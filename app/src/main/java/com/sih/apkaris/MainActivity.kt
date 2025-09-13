package com.sih.apkaris

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.sih.apkaris.fragements.*
import com.sih.apkaris.fragments.BLEFragment
import me.ibrahimsn.lib.SmoothBottomBar
import androidx.core.content.edit

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
            bottomBar.itemActiveIndex = 1
        } else {
            // Not logged in → show login/register without bottom bar
            if (savedInstanceState == null) {
                showLoginFragment()
            }

            // Prepopulate test accounts
            if (!sharedPref.contains("test1@gmail.com_password")) {
                sharedPref.edit().apply {
                    putString("test1@gmail.com_password", "password1")
                    putString("test2@gmail.com_password", "password2")
                    putString("karansingh73457@gmail.com_password", "Karanhere")
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
        sharedPref.edit { putString("loggedInEmail", email) }

        showHomeFragment()
        showBottomBar()
        bottomBar.itemActiveIndex = 1 // highlight Home tab
    }


    private fun showBottomBar() {
        bottomBar.visibility = View.VISIBLE
    }
}
