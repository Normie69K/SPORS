package com.sih.apkaris.fragements

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.sih.apkaris.MainActivity
import com.sih.apkaris.R
import com.sih.apkaris.network.LoginRequest
import com.sih.apkaris.network.RetrofitClient
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnContinue: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // --- FIXED --- Correctly finding the views by their proper IDs from the XML
        etUsername = view.findViewById(R.id.etUsername)
        etPassword = view.findViewById(R.id.etPassword)
        btnContinue = view.findViewById(R.id.btnContinue)

        val tvSignUp = view.findViewById<TextView>(R.id.tvSignUp)
        val tvSignUpHint = view.findViewById<TextView>(R.id.tvSignUpHint)

        // --- ADDED --- Click listener for the top "Sign Up" toggle
        tvSignUp.setOnClickListener {
            (activity as? MainActivity)?.showRegisterFragment()
        }

        // Listener for the bottom "Don't have an account?" text
        tvSignUpHint.setOnClickListener {
            (activity as? MainActivity)?.showRegisterFragment()
        }

        btnContinue.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginUser(username, password)
        }
        return view
    }

    private fun loginUser(username: String, password: String) {
        lifecycleScope.launch {
            try {
                val request = LoginRequest(username = username, password = password)
                val response = RetrofitClient.instance.loginUser(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    val loginData = response.body()!!

                    val sharedPref = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("token", loginData.token)
                        putString("username", username) // Save the username
                        // Save the list of devices as a JSON string
                        putString("devices", Gson().toJson(loginData.user?.devices))
                        apply()
                    }

                    Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.showHomeUI()

                } else {
                    val errorMessage = response.body()?.message ?: "Invalid credentials."
                    Toast.makeText(requireContext(), "Login Failed: $errorMessage", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("LoginFragment", "API call failed", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}