package com.sih.apkaris.fragements

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
import com.sih.apkaris.MainActivity
import com.sih.apkaris.R
import com.sih.apkaris.network.RegisterRequest
import com.sih.apkaris.network.RetrofitClient
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etContact: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvLoginHint: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        etUsername = view.findViewById(R.id.etUsername)
        etContact = view.findViewById(R.id.etContact)
        etAddress = view.findViewById(R.id.etAddress)
        etPassword = view.findViewById(R.id.etPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        tvLoginHint = view.findViewById(R.id.tvLoginHint)

        btnRegister.setOnClickListener {
            if (validateInput()) {
                registerUser()
            }
        }
        tvLoginHint.setOnClickListener { (activity as? MainActivity)?.showLoginFragment() }
        return view
    }

    private fun validateInput(): Boolean {
        if (etUsername.text.toString().isBlank()) {
            Toast.makeText(requireContext(), "Username is required", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etPassword.text.toString().isBlank() || etPassword.text.toString().length < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun registerUser() {
        lifecycleScope.launch {
            try {
                val request = RegisterRequest(
                    username = etUsername.text.toString().trim(),
                    contact = etContact.text.toString().trim().takeIf { it.isNotEmpty() },
                    address = etAddress.text.toString().trim().takeIf { it.isNotEmpty() },
                    password = etPassword.text.toString().trim()
                )

                val response = RetrofitClient.instance.registerUser(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Registration successful! Please log in.", Toast.LENGTH_LONG).show()
                    (activity as? MainActivity)?.showLoginFragment()
                } else {
                    val errorMessage = response.body()?.message ?: "Registration failed."
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("RegisterFragment", "API call failed", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}