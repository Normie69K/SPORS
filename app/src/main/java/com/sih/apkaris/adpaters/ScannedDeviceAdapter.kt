package com.sih.apkaris.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sih.apkaris.bluetooth.ScannedDevice
import com.sih.apkaris.databinding.ListItemScannedDeviceBinding

class ScannedDeviceAdapter(private var devices: List<ScannedDevice>) :
    RecyclerView.Adapter<ScannedDeviceAdapter.DeviceViewHolder>() {

    inner class DeviceViewHolder(val binding: ListItemScannedDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(device: ScannedDevice) {
            binding.tvDeviceName.text = device.name ?: "Unknown Device"
            binding.tvDeviceDetails.text = "Address: ${device.address}, RSSI: ${device.rssi}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ListItemScannedDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount() = devices.size

    fun updateDevices(newDevices: List<ScannedDevice>) {
        this.devices = newDevices
        notifyDataSetChanged()
    }
}