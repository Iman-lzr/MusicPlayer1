package com.example.musicplayer2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splashscreen)

        val splashImage = findViewById<ImageView>(R.id.splash)
        val titleText = findViewById<TextView>(R.id.title)


        val logoSplash = AnimationUtils.loadAnimation(this, R.anim.logo_animation)
        splashImage.startAnimation(logoSplash)


        val titleSplash = AnimationUtils.loadAnimation(this, R.anim.title_animation)
        titleText.startAnimation(titleSplash)


        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 4000)
    }
}
