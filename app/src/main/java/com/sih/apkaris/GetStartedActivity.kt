package com.sih.apkaris

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GetStartedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("appPrefs", MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("isFirstRun", true)

        if (!isFirstRun) {
            // Already seen GetStarted, go directly to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_get_started)

        val btnSignUp: Button = findViewById(R.id.textView7)
        val btnRegister: TextView = findViewById(R.id.textView4)

        btnSignUp.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("showFragment", "login")
            })
            prefs.edit().putBoolean("isFirstRun", false).apply()
            finish()
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("showFragment", "register")
            })
            prefs.edit().putBoolean("isFirstRun", false).apply()
            finish()
        }
    }
}

