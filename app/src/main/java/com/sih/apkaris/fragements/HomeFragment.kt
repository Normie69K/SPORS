package com.sih.apkaris.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sih.apkaris.MainActivity
import com.sih.apkaris.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = activity as? MainActivity

        binding.buttonReportLost.setOnClickListener {
            mainActivity?.showReportLostFragment()
        }
        binding.buttonFindMyDevice.setOnClickListener {
            mainActivity?.showFindMyDeviceFragment()
        }
        binding.buttonHelpFind.setOnClickListener {
            mainActivity?.showHelpFindFragment()
        }
        binding.buttonReportFound.setOnClickListener {
            mainActivity?.showReportFoundFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}