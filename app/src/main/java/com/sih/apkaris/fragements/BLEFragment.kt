package com.sih.apkaris.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.sih.apkaris.R

class BLEFragment : Fragment() {

    private lateinit var ivBluetoothToggle: ImageView
    private lateinit var tvBleStatus: TextView
    private var isBleActive = false  // Track current state

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ble, container, false)

        ivBluetoothToggle = view.findViewById(R.id.ivBluetoothToggle)
        tvBleStatus = view.findViewById(R.id.tvBleStatus)

        ivBluetoothToggle.setOnClickListener {
            toggleBluetooth()
        }

        return view
    }

    private fun toggleBluetooth() {
        if (isBleActive) {
            // Stop BLE
            stopBle()
        } else {
            // Start BLE
            startBle()
        }
    }

    private fun startBle() {
        // TODO: Replace with actual BLE start logic
        isBleActive = true
        ivBluetoothToggle.setImageResource(R.drawable.ic_ble_active)
        ivBluetoothToggle.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.blue)
        )

        tvBleStatus.text = "Bluetooth Running"
    }

    private fun stopBle() {
        // TODO: Replace with actual BLE stop logic
        isBleActive = false
        ivBluetoothToggle.setImageResource(R.drawable.ic_ble_offline)
        ivBluetoothToggle.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.grey)
        )

        tvBleStatus.text = "Bluetooth Stopped"
    }
}
