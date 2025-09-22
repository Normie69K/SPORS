package com.sih.apkaris.fragements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.sih.apkaris.MainActivity
import com.sih.apkaris.databinding.FragmentVerificationBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VerificationFragment : Fragment() {

    private var _binding: FragmentVerificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonVerify.setOnClickListener {
            val otp = binding.otpBox1.text.toString() +
                    binding.otpBox2.text.toString() +
                    binding.otpBox3.text.toString() +
                    binding.otpBox4.text.toString()

            if (otp.length == 4) {
                verifyCode(otp)
            } else {
                Toast.makeText(requireContext(), "Please enter the complete code", Toast.LENGTH_SHORT).show()
            }
        }

        binding.textViewResendLink.setOnClickListener {
            // TODO: Add API call to resend code
            Toast.makeText(requireContext(), "Code Resent!", Toast.LENGTH_SHORT).show()
        }

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun verifyCode(otp: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                // TODO: Add API call here to verify the OTP
                // For demonstration, we'll just use a delay
                delay(1500)

                Toast.makeText(requireContext(), "Code Verified!", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.showCreateNewPasswordFragment()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Verification failed.", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonVerify.isEnabled = false
        } else {
            binding.progressBar.visibility = View.GONE
            binding.buttonVerify.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}