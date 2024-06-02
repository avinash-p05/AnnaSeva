package com.techelites.annaseva.ngo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.techelites.annaseva.R
import com.techelites.annaseva.hotel.Home
import com.techelites.annaseva.hotel.Notification
import com.techelites.annaseva.hotel.Profile

class NgoMainActivity : AppCompatActivity() {
    private lateinit var bottomBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ngo_main)

        bottomBar = findViewById(R.id.bnViewNgo)
        loadFrag(Home())
        bottomBar.setOnItemSelectedListener{
                menuItem ->
            when(menuItem.itemId){
                R.id.nav_home ->{
                    loadFrag(Home())
                    true
                }
                R.id.nav_post ->{
                    loadFrag(NgoFoodListings())
                    true
                }

                R.id.nav_notification -> {
                    loadFrag(Notification())
                    true
                }
                R.id.nav_profile -> {
                    loadFrag((Profile()))
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