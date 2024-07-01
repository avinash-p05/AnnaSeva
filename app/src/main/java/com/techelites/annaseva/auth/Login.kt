package com.techelites.annaseva.auth

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.techelites.annaseva.MainActivity
import com.techelites.annaseva.R
import com.techelites.annaseva.services.RetrofitClient
import com.techelites.annaseva.hotel.HotelMainActivity
import com.techelites.annaseva.ngo.NgoMainActivity
import com.techelites.annaseva.volunteer.VolunteerMainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

    private lateinit var login: Button
    private lateinit var LtoR: TextView
    private lateinit var email: EditText
    private lateinit var pass: EditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioHotel: RadioButton
    private lateinit var radioNgo: RadioButton
    private lateinit var radioVolunteer: RadioButton

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

        LtoR.setOnClickListener {
            val intent2 = Intent(Intent.ACTION_VIEW, Uri.parse("http://annaseva.ajinkyatechnologies.in/api/register"))
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

//        if (emailText.isBlank() || passText.isBlank()) {
//            Toast.makeText(this, "Please fill the fields!!", Toast.LENGTH_SHORT).show()
//            return
//        }

        val selectedRoleId = radioGroup.checkedRadioButtonId
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select a role!", Toast.LENGTH_SHORT).show()
            return
        }

        val role = when (selectedRoleId) {
            R.id.radioHotel -> "hotel"
            R.id.radioNgo -> "ngo"
            R.id.radioVolunteer -> "volunteer"
            else -> "user"
        }

        val baseUrl = "http://annaseva.ajinkyatechnologies.in/api/"
        val loginRequest = LoginRequest(emailText, passText, role)
        val loginRequestV = LoginRequestVolunteer(emailText, passText)
        Log.d("Login", "Request Body: $loginRequestV")

        val apiService = RetrofitClient.getClient(baseUrl)

        val token = "your_token_here" // Replace with actual token

        when (role) {
            "hotel" -> apiService.loginHotel(loginRequest)
                .enqueue(object : Callback<LoginResponseHotel> {
                    override fun onResponse(
                        call: Call<LoginResponseHotel>,
                        response: Response<LoginResponseHotel>
                    ) {
                        handleHotelResponse(response, token)
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
                    handleNgoResponse(response, token)
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
                        handleVolunteerResponse(response, token)
                    }

                    override fun onFailure(call: Call<LoginResponseVolunteer>, t: Throwable) {
                        handleError(t)
                    }
                })

            else -> apiService.loginUser(loginRequest)
                .enqueue(object : Callback<LoginResponseUser> {
                    override fun onResponse(
                        call: Call<LoginResponseUser>,
                        response: Response<LoginResponseUser>
                    ) {
                        handleUserResponse(response, token)
                    }

                    override fun onFailure(call: Call<LoginResponseUser>, t: Throwable) {
                        handleError(t)
                    }
                })
        }
    }

    private fun handleHotelResponse(response: Response<LoginResponseHotel>, token: String) {
        if (response.isSuccessful && response.body() != null) {
            val user = response.body()?.user
            Log.d("Login", "Hotel Response: ${response.body()}")
            user?.let {
                saveHotelData(it, "hotel", token)
                Toast.makeText(this@Login, "Welcome ${it.name}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@Login, HotelMainActivity::class.java))
                finish()
            }
        } else {
            handleErrorResponse(response)
        }
    }

    private fun handleNgoResponse(response: Response<LoginResponseNgo>, token: String) {
        if (response.isSuccessful && response.body() != null) {
            val user = response.body()?.user
            Log.d("Login", "NGO Response: ${response.body()}")
            user?.let {
                saveNgoData(it, "ngo", token)
                Toast.makeText(this@Login, "Welcome ${it.name}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@Login, NgoMainActivity::class.java))
                finish()
            }
        } else {
            handleErrorResponse(response)
        }
    }

    private fun handleVolunteerResponse(response: Response<LoginResponseVolunteer>, token: String) {
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

    private fun handleUserResponse(response: Response<LoginResponseUser>, token: String) {
        if (response.isSuccessful && response.body() != null) {
            val user = response.body()?.user
            Log.d("Login", "User Response: ${response.body()}")
            user?.let {
                saveUserData(it, "user", token)
                Toast.makeText(this@Login, "Welcome ${it.fullName}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@Login, MainActivity::class.java))
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

    private fun saveUserData(user: User, role: String, token: String) {
        val pref: SharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        with(pref.edit()) {
            putBoolean("flag", true)
            putString("userid", user.email)
            putString("username", user.fullName)
            putString("email", user.email)
            putString("userType", role)
            putString("token", token)
            apply()
        }
    }

    private fun saveHotelData(user: Hotel, role: String, token: String) {
        val pref: SharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        with(pref.edit()) {
            putBoolean("flag", true)
            putString("userid", user._id)
            putString("username", user.name)
            putString("email", user.email)
            putString("userType", role)
            putString("contactPerson", user.contactPerson)
            putString("token", token)
            apply()
        }
    }

    private fun saveNgoData(user: Ngo, role: String, token: String) {
        val pref: SharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        with(pref.edit()) {
            putBoolean("flag", true)
            putString("userid", user._id)
            putString("username", user.name)
            putString("email", user.email)
            putString("userType", role)
            putString("token", token)
            putString("address", user.address)
            putString("city", user.city)
            putString("state", user.state)
            putString("pincode", user.pincode)
            putString("contactPerson", user.contactPerson)
            putString("contactNumber", user.contactNumber)
            apply()
        }
    }

    private fun saveVolunteerData(user: Volunteer, role: String, token: String) {
        val pref: SharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        with(pref.edit()) {
            putBoolean("flag", true)
            putString("userid", user._id)
            putString("username", user.name)
            putString("email", user.email)
            putString("userType", role)
            putString("token", token)
            putString("address", user.address)
            putString("city", user.city)
            putString("state", user.state)
            putString("pincode", user.pincode)
            putString("contactNumber", user.contactNumber)
            apply()
        }
    }
}
