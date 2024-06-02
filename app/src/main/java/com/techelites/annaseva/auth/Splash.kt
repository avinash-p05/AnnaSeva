package com.techelites.annaseva.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.techelites.annaseva.MainActivity
import com.techelites.annaseva.R
import com.techelites.annaseva.hotel.HotelMainActivity
import com.techelites.annaseva.ngo.NgoMainActivity
import com.techelites.annaseva.volunteer.VolunteerMainActivity

class Splash : AppCompatActivity() {

    private lateinit var uploadAnimation : LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        uploadAnimation = findViewById(R.id.loadingAnimation)
        uploadAnimation.playAnimation()
        val intentStart = Intent(this, Start::class.java)

        val intentMain = Intent(this, MainActivity::class.java)

        val intentHotel = Intent(this, HotelMainActivity::class.java)

        val intentNgo = Intent(this, NgoMainActivity::class.java)


        val intentVolunteer = Intent(this, VolunteerMainActivity::class.java)


        val pref: SharedPreferences = getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
        val flag : Boolean = pref.getBoolean("flag",false)
        val role : String ?= pref.getString("userType","");

        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable{
            if(flag && role=="hotel"){
                uploadAnimation.cancelAnimation()
                startActivity(intentHotel)
            }
            else if(flag && role=="ngo"){
                uploadAnimation.cancelAnimation()
                startActivity(intentNgo)
            }
            else if(flag && role=="volunteer"){
                uploadAnimation.cancelAnimation()
                startActivity(intentVolunteer)
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