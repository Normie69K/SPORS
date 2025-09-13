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

class LoginFragment : Fragment() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnContinue: Button
    private lateinit var tvSignUp: TextView
    private lateinit var tvSignUpHint: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnContinue = view.findViewById(R.id.btnContinue)
        tvSignUp = view.findViewById(R.id.tvSignUp)
        tvSignUpHint = view.findViewById(R.id.tvSignUpHint)

        btnContinue.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (!isValidEmail(email)) {
                Toast.makeText(requireContext(), "Enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty() || password.length > 50) {
                Toast.makeText(requireContext(), "Password must be 1-50 chars", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val sharedPref = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().apply {
                putString("test1@gmail.com_password", "password1")
                putString("test2@gmail.com_password", "password2")
                putString("admin@gmail.com_password", "admin")
                putString("karansingh73457@gmail.com_password", "Karanhere")
                apply()
            }

            val storedPassword = sharedPref.getString("${email}_password", null)
            if (storedPassword != null && storedPassword == password) {
                (activity as MainActivity).goToHome(email)
            } else {
                Toast.makeText(requireContext(), "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        tvSignUp.setOnClickListener { (activity as? MainActivity)?.showRegisterFragment() }
        tvSignUpHint.setOnClickListener { (activity as? MainActivity)?.showRegisterFragment() }

        return view
    }

    private fun isValidEmail(email: String) =
        email.isNotEmpty() && email.length <= 50 && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

