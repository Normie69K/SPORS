package com.sih.apkaris.fragements

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.sih.apkaris.MainActivity
import com.sih.apkaris.R

class RegisterFragment : Fragment() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var tvLoginHint: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        tvLogin = view.findViewById(R.id.tvLogin)
        tvLoginHint = view.findViewById(R.id.tvLoginHint)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm = etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || name.length > 50) {
                Toast.makeText(requireContext(), "Name must be 1-50 chars", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isValidEmail(email)) {
                Toast.makeText(requireContext(), "Enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty() || password.length > 50) {
                Toast.makeText(requireContext(), "Password must be 1-50 chars", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirm) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPref = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().apply {
                putString("${email}_name", name)
                putString("${email}_password", password)
                apply()
            }

            Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
            (activity as MainActivity).goToHome(email)
        }

        tvLogin.setOnClickListener { (activity as? MainActivity)?.showLoginFragment() }
        tvLoginHint.setOnClickListener { (activity as? MainActivity)?.showLoginFragment() }

        return view
    }

    private fun isValidEmail(email: String) =
        email.isNotEmpty() && email.length <= 50 && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

