package com.sih.apkaris.fragements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sih.apkaris.databinding.FragmentReportFoundBinding

class ReportFoundFragment : Fragment() {

    private var _binding: FragmentReportFoundBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportFoundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonReportFound.setOnClickListener {
            // TODO: Create a dialog or new screen to enter the found device's ID
            Toast.makeText(requireContext(), "Report Found clicked", Toast.LENGTH_SHORT).show()
        }

        binding.buttonPastReports.setOnClickListener {
            // TODO: Navigate to a screen showing a list of past reports
            Toast.makeText(requireContext(), "Past Reports clicked", Toast.LENGTH_SHORT).show()
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