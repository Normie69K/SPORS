package com.sih.apkaris.fragements

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.sih.apkaris.MainActivity
import com.sih.apkaris.databinding.FragmentSignUpBinding
import com.sih.apkaris.network.RegisterRequest
import com.sih.apkaris.network.RetrofitClient
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCreateAccount.setOnClickListener {
            if (validateInput()) {
                registerUser()
            }
        }

        binding.textViewLoginLink.setOnClickListener {
            (activity as? MainActivity)?.showLoginFragment()
        }
    }

    private fun validateInput(): Boolean {
        if (binding.editTextEmail.text.toString().isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(binding.editTextEmail.text.toString()).matches()) {
            binding.inputLayoutEmail.error = "Enter a valid email"
            return false
        } else {
            binding.inputLayoutEmail.error = null
        }

        if (binding.editTextPassword.text.toString().length < 6) {
            binding.inputLayoutPassword.error = "Password must be at least 6 characters"
            return false
        } else {
            binding.inputLayoutPassword.error = null
        }
        return true
    }

    private fun registerUser() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        lifecycleScope.launch {
            try {
                val request = RegisterRequest(username = email, contact = null, address = null, password = password)
                val response = RetrofitClient.instance.registerUser(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Registration successful! Please log in.", Toast.LENGTH_LONG).show()
                    (activity as? MainActivity)?.showLoginFragment()
                } else {
                    val errorMessage = response.body()?.message ?: "Registration failed."
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("SignUpFragment", "API call failed", e)
                Toast.makeText(requireContext(), "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}