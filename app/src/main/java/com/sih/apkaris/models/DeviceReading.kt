package com.sih.apkaris.models

data class NearbyDevice(
    val id: String,
    val rssi: Int
)

data class BlePayload(
    val deviceId: String,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long,
    val nearby: List<NearbyDevice>
)
