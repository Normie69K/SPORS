package com.sih.apkaris.fragements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sih.apkaris.MainActivity
import com.sih.apkaris.databinding.FragmentVerificationBinding

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
            // TODO: Add logic to collect OTP and call API to verify
            Toast.makeText(requireContext(), "Code Verified!", Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.showCreateNewPasswordFragment()
        }

        binding.textViewResendLink.setOnClickListener {
            // TODO: Add API call to resend code
            Toast.makeText(requireContext(), "Code Resent!", Toast.LENGTH_SHORT).show()
        }

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}