package com.sih.apkaris

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity: AppCompatActivity()  {

        private lateinit var etName: TextInputEditText
        private lateinit var etEmail: TextInputEditText
        private lateinit var etPassword: TextInputEditText
        private lateinit var btnRegister: Button
        private lateinit var btnGoToLogin: TextView
        private lateinit var btnGoToLoginHint: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_register)

            etName = findViewById(R.id.etName)
            etEmail = findViewById(R.id.etEmail)
            etPassword = findViewById(R.id.etPassword)
            btnRegister = findViewById(R.id.btnRegister)
            btnGoToLoginHint = findViewById(R.id.tvLoginHint) // button for navigating to login
            btnGoToLogin = findViewById(R.id.tvLogin) // button for navigating to login

            btnRegister.setOnClickListener {
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                // Add your registration logic here
            }


            btnGoToLogin.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }

            btnGoToLoginHint.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }
    }
