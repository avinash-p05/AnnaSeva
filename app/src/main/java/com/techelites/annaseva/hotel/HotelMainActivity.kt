package com.techelites.annaseva.hotel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.techelites.annaseva.R

class HotelMainActivity : AppCompatActivity() {
    private lateinit var bottomBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotel_main)

        bottomBar = findViewById(R.id.bnViewHotel)
        loadFrag(HotelFoodListings())
        bottomBar.setOnItemSelectedListener{
                menuItem ->
            when(menuItem.itemId){
                R.id.nav_home ->{
                    loadFrag(HotelFoodListings())
                    true
                }
                R.id.nav_listing ->{
                    loadFrag(AcceptedRequestsListings())
                    true
                }
                R.id.nav_post ->{
                    loadFrag(Post())
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