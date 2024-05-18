package com.techelites.annaseva

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomBar = findViewById(R.id.bnView)
        loadFrag(Home())
        bottomBar.setOnItemSelectedListener{
                menuItem ->
            when(menuItem.itemId){
                R.id.nav_home ->{
                    loadFrag(Home())
                    true
                }
                R.id.nav_post->{
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