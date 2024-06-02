package com.techelites.annaseva.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.techelites.annaseva.R

class Start : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val start : Button = findViewById(R.id.startbtn)
        val intent = Intent(this, Login::class.java)
        start.setOnClickListener(View.OnClickListener {
            startActivity(intent)
        })

    }
}