package com.techelites.annaseva.volunteer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.techelites.annaseva.R
import com.techelites.annaseva.hotel.Home
import com.techelites.annaseva.hotel.HotelDashboard
import com.techelites.annaseva.hotel.Notification
import com.techelites.annaseva.hotel.Profile

class VolunteerMainActivity : AppCompatActivity() {
    private lateinit var bottomBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer_main)

        bottomBar = findViewById(R.id.bnViewNgo)
        loadFrag(HotelDashboard())
        bottomBar.setOnItemSelectedListener{
                menuItem ->
            when(menuItem.itemId){
                R.id.nav_home ->{
                    loadFrag(HotelDashboard())
                    true
                }
                R.id.nav_post ->{
                    loadFrag(VolunteerFoodListings())
                    true
                }

                R.id.nav_notification -> {
                    loadFrag(VolunNotifications())
                    true
                }
                R.id.nav_profile -> {
                    loadFrag((VolunteerProfile()))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFrag(fragment : Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container,fragment)
        transaction.commit()
    }
}