package com.techelites.annaseva

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Start : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val start : Button = findViewById(R.id.startbtn)
        val intent = Intent(this,Login::class.java)
        start.setOnClickListener(View.OnClickListener {
            startActivity(intent)
        })

    }
}