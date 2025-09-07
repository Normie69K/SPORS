package com.sih.apkaris

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Splash_Screen : AppCompatActivity() {

    //Variables
    private lateinit var topanim :Animation
    private lateinit var bottomanim :Animation
    private lateinit var image :ImageView
    private lateinit var name : TextView
    private lateinit var description : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Animations
        topanim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomanim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        //Hooks
        image = findViewById(R.id.app_icon)
        name = findViewById(R.id.apk_name)
        description = findViewById(R.id.app_description)

        //Animation
        image.startAnimation(topanim)
        name.startAnimation(topanim)
        description.startAnimation(bottomanim)

        //Delay 5 sec -> then go to the main_activity
        lifecycleScope.launch {
            delay(3000)
            startActivity(Intent(this@Splash_Screen,LoginActivity::class.java))
            finish() //destroy this activity
        }

    }
}