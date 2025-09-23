package com.sih.apkaris.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.sih.apkaris.MainActivity
import com.sih.apkaris.R
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

        updateThemeIcon()

        binding.buttonThemeSwitcher.setOnClickListener {
            toggleTheme()
        }


//        binding.fabThemeSwitcher.setOnClickListener {
//            // Check what the current night mode is
//            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
//
//            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
//                // If it's currently night, switch to day mode
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//            } else {
//                // If it's currently day, switch to night mode
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//            }
//        }


        binding.buttonSettings.setOnClickListener {
            mainActivity?.showProfileFragment()
        }
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

    private fun toggleTheme() {
        // Check what the current night mode is
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // If it's currently night, switch to day mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            // If it's currently day, switch to night mode
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun updateThemeIcon() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // It's dark mode, show the sun icon to switch to light
            binding.buttonThemeSwitcher.setImageResource(R.drawable.ic_sun)
        } else {
            // It's light mode, show the moon icon to switch to dark
            binding.buttonThemeSwitcher.setImageResource(R.drawable.ic_moon)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}