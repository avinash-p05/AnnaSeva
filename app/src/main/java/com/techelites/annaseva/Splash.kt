package com.techelites.annaseva

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView

class Splash : AppCompatActivity() {

    private lateinit var uploadAnimation : LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        uploadAnimation = findViewById(R.id.loadingAnimation)
        uploadAnimation.playAnimation()
        val intentStart = Intent(this,Start::class.java)
        val intentMain = Intent(this,MainActivity::class.java)

        val pref: SharedPreferences = getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
        val flag : Boolean = pref.getBoolean("flag",false)

        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable{
            if(flag){
                uploadAnimation.cancelAnimation()
                startActivity(intentMain)
            }
            else {
                uploadAnimation.cancelAnimation()
                startActivity(intentStart)
            }
            finish()
        }
        handler.postDelayed(runnable,3000)

    }
}