package com.sih.apkaris.fragements

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.sih.apkaris.MainActivity
import com.sih.apkaris.databinding.FragmentLoginBinding
import com.sih.apkaris.network.LoginRequest
import com.sih.apkaris.network.RetrofitClient
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginUser(email, password)
        }

        binding.textViewSignupLink.setOnClickListener {
            (activity as? MainActivity)?.showSignUpFragment()
        }

        binding.textViewForgotPassword.setOnClickListener {
            (activity as? MainActivity)?.showForgotPasswordFragment()
        }
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
                        putString("username", username)
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
                Toast.makeText(requireContext(), "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}