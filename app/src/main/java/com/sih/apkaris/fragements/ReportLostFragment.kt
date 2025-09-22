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
import com.sih.apkaris.databinding.FragmentReportLostBinding
import com.sih.apkaris.network.ReportLostRequest
import com.sih.apkaris.network.RetrofitClient
import kotlinx.coroutines.launch

class ReportLostFragment : Fragment() {

    private var _binding: FragmentReportLostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportLostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonMarkAsLost.setOnClickListener {
            val deviceId = binding.editTextDeviceId.text.toString().trim()
            if (deviceId.isNotEmpty()) {
                reportDeviceAsLost(deviceId)
            } else {
                binding.inputLayoutDeviceId.error = "Device ID cannot be empty"
            }
        }

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun reportDeviceAsLost(deviceId: String) {
        lifecycleScope.launch {
            try {
                // You need a way to get the current user's ID, e.g., from SharedPreferences
                val userId = "user_id_from_prefs"
                val request = ReportLostRequest(deviceId = deviceId, userId = userId)
                val response = RetrofitClient.instance.reportLostDevice(request)

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Device $deviceId has been marked as lost.", Toast.LENGTH_LONG).show()
                    (activity as? MainActivity)?.showHomeUI()
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to report device."
                    Toast.makeText(requireContext(), "Error: $errorMsg", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("ReportLostFragment", "API call failed", e)
                Toast.makeText(requireContext(), "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}