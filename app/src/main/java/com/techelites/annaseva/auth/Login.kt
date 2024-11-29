package com.techelites.annaseva.auth

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.techelites.annaseva.MainActivity
import com.techelites.annaseva.MyFirebaseMessagingService
import com.techelites.annaseva.R
import com.techelites.annaseva.services.RetrofitClient
import com.techelites.annaseva.hotel.HotelMainActivity
import com.techelites.annaseva.ngo.NgoMainActivity
import com.techelites.annaseva.volunteer.VolunteerMainActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class Login : AppCompatActivity() {
    private lateinit var token: String
    private lateinit var login: Button
    private lateinit var LtoR: TextView
    private lateinit var email: EditText
    private lateinit var pass: EditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioHotel: RadioButton
    private lateinit var radioNgo: RadioButton
    private lateinit var radioVolunteer: RadioButton
    private lateinit var messagingService: MyFirebaseMessagingService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login = findViewById(R.id.loginbtn)
        LtoR = findViewById(R.id.LtoR)
        email = findViewById(R.id.email)
        pass = findViewById(R.id.ccpassword)
        radioGroup = findViewById(R.id.radioGroup)
        radioHotel = findViewById(R.id.radioHotel)
        radioNgo = findViewById(R.id.radioNgo)
        radioVolunteer = findViewById(R.id.radioVolunteer)

        messagingService = MyFirebaseMessagingService()

        // Initialize the FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            token = task.result
            Log.d("FCM", "FCM Token: $token")
        }

        LtoR.setOnClickListener {
            val intent2 = Intent(Intent.ACTION_VIEW, Uri.parse("http://10.0.2.2:8080/register"))
            startActivity(intent2)
            finish()
        }

        login.setOnClickListener {
            login()
        }

        findViewById<ImageButton>(R.id.backArrowLogin).setOnClickListener {
            finish()
        }
    }

    private fun login() {
        val emailText = email.text.toString()
        val passText = pass.text.toString()

        if (emailText.isBlank() || passText.isBlank()) {
            Toast.makeText(this, "Please fill the fields!!", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRoleId = radioGroup.checkedRadioButtonId
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select a role!", Toast.LENGTH_SHORT).show()
            return
        }

        val role = when (selectedRoleId) {
            R.id.radioHotel -> "hotel"
            R.id.radioNgo -> "ngo"
            else -> "volunteer"
        }

//        val baseUrl = "http://10.0.2.2:8080/"
        val baseUrl = "https://anna-seva-backend.onrender.com/"
        val loginRequest = LoginRequest(emailText, passText)
        val loginRequestV = LoginRequestVolunteer(emailText, passText)
        Log.d("Login", "Request Body: $loginRequestV")

        val apiService = RetrofitClient.getClient(baseUrl)

        when (role) {
            "hotel" -> apiService.loginHotel(loginRequest)
                .enqueue(object : Callback<LoginResponseHotel> {
                    override fun onResponse(
                        call: Call<LoginResponseHotel>,
                        response: Response<LoginResponseHotel>
                    ) {
                        handleHotelResponse(response)
                    }

                    override fun onFailure(call: Call<LoginResponseHotel>, t: Throwable) {
                        handleError(t)
                    }
                })

            "ngo" -> apiService.loginNgo(loginRequest).enqueue(object : Callback<LoginResponseNgo> {
                override fun onResponse(
                    call: Call<LoginResponseNgo>,
                    response: Response<LoginResponseNgo>
                ) {
                    handleNgoResponse(response)
                }

                override fun onFailure(call: Call<LoginResponseNgo>, t: Throwable) {
                    handleError(t)
                }
            })

            "volunteer" -> apiService.loginVolunteer(loginRequestV)
                .enqueue(object : Callback<LoginResponseVolunteer> {
                    override fun onResponse(
                        call: Call<LoginResponseVolunteer>,
                        response: Response<LoginResponseVolunteer>
                    ) {
                        val rawJson = response.errorBody()?.string() ?: response.body().toString()
                        Log.d("Login", "Raw JSON Response: $rawJson")
                        handleVolunteerResponse(response)
                    }

                    override fun onFailure(call: Call<LoginResponseVolunteer>, t: Throwable) {
                        handleError(t)
                    }
                })
        }
    }

    private fun handleHotelResponse(response: Response<LoginResponseHotel>) {
        if (response.isSuccessful && response.body() != null) {
            val data = response.body()?.data
            Log.d("Login", "Hotel Response: ${response.body()}")
            data?.let {
                saveHotelData(it, "hotel")
                Toast.makeText(this@Login, "Welcome ${it.name}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@Login, HotelMainActivity::class.java))
                finish()
            }
        } else {
            handleErrorResponse(response)
        }
    }

    private fun handleNgoResponse(response: Response<LoginResponseNgo>) {
        if (response.isSuccessful && response.body() != null) {
            val data = response.body()?.data
            Log.d("Login", "NGO Response: ${response.body()}")
            data?.let {
                saveNgoData(it, "ngo")
                Toast.makeText(this@Login, "Welcome ${it.name}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@Login, NgoMainActivity::class.java))
                finish()
            }
        } else {
            handleErrorResponse(response)
        }
    }

    private fun handleVolunteerResponse(response: Response<LoginResponseVolunteer>) {
        if (response.isSuccessful && response.body() != null) {
            val user = response.body()?.volunteer
            Log.d("Login", "Volunteer Response: ${response.body()}")
            user?.let {
                saveVolunteerData(it, "volunteer", token)
                Toast.makeText(this@Login, "Welcome ${it.name}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@Login, VolunteerMainActivity::class.java))
                finish()
            }
        } else {
            handleErrorResponse(response)
        }
    }

    private fun handleError(t: Throwable) {
        Log.e("Login", "Login failed", t)
        Toast.makeText(this, "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
    }

    private fun handleErrorResponse(response: Response<*>) {
        Log.d("Login", "Response Code: ${response.code()}")
        Log.d("Login", "Response Message: ${response.message()}")
        Log.d("Login", "Error Body: ${response.errorBody()?.string()}")
        Toast.makeText(this, "Account Doesn't exist!! Create one", Toast.LENGTH_SHORT).show()
    }

    private fun saveHotelData(user: Hotel, role: String) {
        val pref: SharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        with(pref.edit()) {
            putBoolean("flag", true)
            putString("userId", user.id)
            putString("token", token)
            putString("userName", user.name)
            putString("email", user.email)
            putString("userType", role)
            putString("address", user.address)
            putString("city", user.city)
            putString("location", user.location.coordinates.toString())
            putString("state", user.state)
            putString("pinCode", user.pinCode)
            putString("contactPerson", user.contactPerson)
            putString("contactNumber", user.contactNumber)
            apply()
        }
        messagingService.sendTokenToServer(token,user.id)
    }

    private fun saveNgoData(user: Ngo, role: String) {
        val pref: SharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        with(pref.edit()) {
            putBoolean("flag", true)
            putString("userId", user.id)
            putString("token", token)
            putString("userName", user.name)
            putString("email", user.email)
            putString("userType", role)
            putString("address", user.address)
            putString("city", user.city)
            putString("location", user.location.coordinates.toString())
            putString("state", user.state)
            putString("pinCode", user.pinCode)
            putString("contactPerson", user.contactPerson)
            putString("contactNumber", user.contactNumber)
            apply()
        }
        messagingService.sendTokenToServer(token,user.id)
    }

    private fun saveVolunteerData(user: Volunteer, role: String, token: String) {
        val pref: SharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        with(pref.edit()) {
            putBoolean("flag", true)
            putString("token", token)
            putString("userId", user._id)
            putString("userName", user.name)
            putString("email", user.email)
            putString("userType", role)
            putString("phone", user.contactNumber)
            apply()
        }

        messagingService.sendTokenToServer(token,user._id)
    }



}
