package com.sih.apkaris

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Splash_Screen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to your new splash screen layout
        setContentView(R.layout.activity_splash_screen)

        // Find views by their ID from the new layout
        val appLogo: TextView = findViewById(R.id.app_logo)
        val appDescription: TextView = findViewById(R.id.app_description)

        // Animations
        val topAnim: Animation = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        val bottomAnim: Animation = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        // Start animations
        appLogo.startAnimation(topAnim)
        appDescription.startAnimation(bottomAnim)

        // Delay and navigate
        lifecycleScope.launch {
            delay(3000)
            startActivity(Intent(this@Splash_Screen, GetStartedActivity::class.java))
            finish()
        }
    }
}