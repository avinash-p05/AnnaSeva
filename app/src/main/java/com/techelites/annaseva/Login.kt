package com.techelites.annaseva

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Login : AppCompatActivity() {

    private lateinit var login: Button
    private lateinit var LtoR: TextView
    private lateinit var email: EditText
    private lateinit var pass: EditText
    private lateinit var progressBar: ProgressBar
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://your-backend-url.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val apiService = retrofit.create(ApiService::class.java)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login = findViewById(R.id.loginbtn)
        LtoR = findViewById(R.id.LtoR)
        email = findViewById(R.id.email)
        pass = findViewById(R.id.ccpassword)
//        progressBar = findViewById(R.id.progressBarL)
//        progressBar.visibility = View.INVISIBLE

        val intent2 = Intent(Intent.ACTION_VIEW, Uri.parse("https://your-registration-url.com"))
        LtoR.setOnClickListener {
            startActivity(intent2)
            finish()
        }

        login.setOnClickListener {
            login()
        }

        val arrowBack: ImageButton = findViewById(R.id.backArrowLogin)
        arrowBack.setOnClickListener {
            finish()
        }
    }

    private fun login(){
        val emailText = email.text.toString()
        val passText = pass.text.toString()

        if (emailText.isBlank() || passText.isBlank()) {
            Toast.makeText(this, "Please fill the fields!!", Toast.LENGTH_SHORT).show()
            return
        }
        else if(emailText=="avinash@gmail.com" && passText=="12345"){
            val intent1 = Intent(this@Login, MainActivity::class.java)
            startActivity(intent1)
            finish()
        }
    }

//    private fun login() {
////        progressBar.visibility = View.VISIBLE
//        val emailText = email.text.toString()
//        val passText = pass.text.toString()
//
//        if (emailText.isBlank() || passText.isBlank()) {
//            Toast.makeText(this, "Please fill the fields!!", Toast.LENGTH_SHORT).show()
////            progressBar.visibility = View.GONE
//            return
//        }
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = apiService.login(LoginRequest(emailText, passText))
//                withContext(Dispatchers.Main) {
//                    if (response.isSuccessful) {
//                         val user = response.body()?.user
//                        user?.let {
//                            saveUserData(it)
//                            Toast.makeText(this@Login, "Welcome Chef. ${it.username}", Toast.LENGTH_SHORT).show()
//                            val intent1 = Intent(this@Login, MainActivity::class.java)
//                            startActivity(intent1)
//                            finish()
//                        }
//                    } else {
//                        Toast.makeText(this@Login, "Account Doesn't exist!! Create one ", Toast.LENGTH_SHORT).show()
//                    }
////                    progressBar.visibility = View.GONE
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@Login, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
////                    progressBar.visibility = View.GONE
//                }
//            }
//        }
//    }

    private fun saveUserData(user: User) {
        val pref: SharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putBoolean("flag", true)
        editor.putString("userid", user.id)
        editor.putString("username", user.username)
        editor.putString("email", user.email)
        editor.apply()
    }

}
