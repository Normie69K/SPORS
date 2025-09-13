package com.sih.apkaris.models

data class DeviceData(
    val deviceId: String,
    val latitude: Double,
    val longitude: Double,
    val nearbyDeviceIds: List<String>
)
