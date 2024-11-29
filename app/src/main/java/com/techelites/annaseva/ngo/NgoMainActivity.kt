package com.techelites.annaseva.ngo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.techelites.annaseva.R
import com.techelites.annaseva.hotel.AcceptedRequestsListings

class NgoMainActivity : AppCompatActivity() {
    private lateinit var bottomBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ngo_main)

        bottomBar = findViewById(R.id.bnViewNgo)
        loadFrag(NgoFoodListings())
        bottomBar.setOnItemSelectedListener{
                menuItem ->
            when(menuItem.itemId){
                R.id.nav_home ->{
                    loadFrag(NgoFoodListings())
                    true
                }
                R.id.nav_post ->{
                    loadFrag(RequestedDonationsListings())
                    true
                }

                R.id.nav_accept ->{
                    loadFrag(AcceptedFoodDonationsListings())
                    true
                }

                R.id.nav_notification -> {
                    loadFrag(NgoNotifications())
                    true
                }
                R.id.nav_profile -> {
                    loadFrag((NgoProfile()))
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