package com.sih.apkaris

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.sih.apkaris.fragements.LoginFragment
import com.sih.apkaris.fragements.RegisterFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val showFragment = intent.getStringExtra("showFragment")
        if (showFragment == "register") showRegisterFragment()
        else showLoginFragment()

        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        val loggedInEmail = sharedPref.getString("loggedInEmail", null)

        // If a user is already logged in, go directly to HomeActivity
        if (loggedInEmail != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            // Show login fragment if no user logged in
            if (savedInstanceState == null) {
                supportFragmentManager.commit {
                    replace(R.id.fragmentContainer, LoginFragment())
                }
            }

            // Prepopulate some temporary logins if not done before
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

    fun goToHome(email: String) {
        // Store logged in user
        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("loggedInEmail", email).apply()

        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
