package com.techelites.annaseva.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.messaging.FirebaseMessaging
import com.techelites.annaseva.MainActivity
import com.techelites.annaseva.MyFirebaseMessagingService
import com.techelites.annaseva.R
import com.techelites.annaseva.hotel.HotelMainActivity
import com.techelites.annaseva.ngo.NgoMainActivity
import com.techelites.annaseva.volunteer.VolunteerMainActivity

class Splash : AppCompatActivity() {

    private lateinit var uploadAnimation : LottieAnimationView
    private lateinit var messagingService: MyFirebaseMessagingService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        uploadAnimation = findViewById(R.id.loadingAnimation)
        messagingService = MyFirebaseMessagingService()
        uploadAnimation.playAnimation()


        val intentStart = Intent(this, Start::class.java)

        val intentMain = Intent(this, MainActivity::class.java)

        val intentHotel = Intent(this, HotelMainActivity::class.java)

        val intentNgo = Intent(this, NgoMainActivity::class.java)


        val intentVolunteer = Intent(this, VolunteerMainActivity::class.java)


        val pref: SharedPreferences = getSharedPreferences("login", AppCompatActivity.MODE_PRIVATE)
        val flag: Boolean = pref.getBoolean("flag", false)

        val role: String? = pref.getString("userType", "");
        // Using applicationContext to ensure context is not null
        val userId = pref.getString("userId", null)

        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            if (flag && role == "hotel" && userId!=null) {
                accessToken(userId)
                uploadAnimation.cancelAnimation()
                startActivity(intentHotel)
            } else if (flag && role == "ngo"  && userId!=null) {
                accessToken(userId)
                uploadAnimation.cancelAnimation()
                startActivity(intentNgo)
            } else if (flag && role == "volunteer"  && userId!=null) {
                accessToken(userId)
                uploadAnimation.cancelAnimation()
                startActivity(intentVolunteer)
            } else {
                uploadAnimation.cancelAnimation()
                startActivity(intentStart)
            }
            finish()
        }
        handler.postDelayed(runnable, 3000)
    }

    private fun accessToken(userId:String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get the FCM token
            val token = task.result
            Log.d("FCM", "FCM Token: $token")
           messagingService.onNewToken(token, userId )
            }
        }
    }