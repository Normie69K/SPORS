package com.sih.apkaris

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.sih.apkaris.services.BeaconService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Check if the user is logged in before starting the service
            val sharedPref = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
            val sessionToken = sharedPref.getString("token", null)

            if (sessionToken != null) {
                Log.d("BootReceiver", "User is logged in. Starting BeaconService on boot.")
                val serviceIntent = Intent(context, BeaconService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } else {
                Log.d("BootReceiver", "User is not logged in. Service will not be started.")
            }
        }
    }
}