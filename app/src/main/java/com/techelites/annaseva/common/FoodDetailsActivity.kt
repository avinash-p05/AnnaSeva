package com.techelites.annaseva.common

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonParser
import com.techelites.annaseva.R
import com.techelites.annaseva.ngo.FoodNgo
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class FoodDetailsActivity : AppCompatActivity() {

    private lateinit var foodName: TextView
    private lateinit var foodCategory: TextView
    private lateinit var foodQuantity: TextView
    private lateinit var foodExpiry: TextView
    private lateinit var foodIdealFor: TextView
    private lateinit var foodAvailableAt: TextView
    private lateinit var foodTransportation: TextView
    private lateinit var foodContactPerson: TextView
    private lateinit var foodDonationStatus: TextView
    private lateinit var foodPickupInstructions: TextView
    private lateinit var foodHotelName: TextView
    private lateinit var foodDescription: TextView
    private lateinit var foodCreatedAt: TextView
    private lateinit var foodType : TextView
    private lateinit var foodLocation: TextView
    private lateinit var requestButton: Button

    private val openCageApiKey = "3216512d44244bf1acd0fd1398aa2652"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_details)

        foodName = findViewById(R.id.foodName)
        foodType = findViewById(R.id.typeD)
        foodCategory = findViewById(R.id.foodCategory)
        foodQuantity = findViewById(R.id.foodQuantity)
        foodExpiry = findViewById(R.id.foodExpiry)
        foodIdealFor = findViewById(R.id.foodIdealFor)
        foodAvailableAt = findViewById(R.id.foodAvailableAt)
        foodTransportation = findViewById(R.id.foodTransportation)
        foodContactPerson = findViewById(R.id.foodContactPerson)
        foodDonationStatus = findViewById(R.id.foodDonationStatus)
        foodPickupInstructions = findViewById(R.id.foodinstruction)
        foodHotelName = findViewById(R.id.foodHotelName)
        foodDescription = findViewById(R.id.fooddescription)
//        foodCreatedAt = findViewById(R.id.foodAvailableAt)
        foodLocation = findViewById(R.id.foodLocation)
        requestButton = findViewById(R.id.requestButton)

        val foodItem = intent.getSerializableExtra("foodItem") as? FoodNgo
        foodItem?.let { updateUI(it) }

        requestButton.setOnClickListener {
            foodItem?.let { makeRequest(it) }
        }

        findViewById<ImageButton>(R.id.backArrowDetials).setOnClickListener {
            finish()
        }
    }

    private fun updateUI(food: FoodNgo) {
        // Update UI with food details
        foodType.text = food.type
        foodName.text = food.name
        foodCategory.text = food.category
        foodQuantity.text = food.quantity.toString()
        foodExpiry.text = food.expiry.toString()
        foodIdealFor.text = food.idealfor
        foodAvailableAt.text = food.availableAt
        foodTransportation.text = food.transportation
        foodContactPerson.text = food.contactPerson
        foodDonationStatus.text = food.donationStatus
        foodPickupInstructions.text = food.pickupInstructions
        foodHotelName.text = food.hotelName
        foodDescription.text = food.description
//        foodCreatedAt.text = food.createdAt
        foodLocation.text = "Loading location..." // Placeholder until geocoding is done
        food.location?.let { geocodeLocation(it) }

    }

    private fun geocodeLocation(location: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.opencagedata.com/geocode/v1/json?q=${location}&key=${openCageApiKey}")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FoodDetailsActivity", "Geocoding failed: ${e.message}")
                runOnUiThread {
                    foodLocation.text = "Geocoding failed"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val responseString = responseBody.string()
                        val jsonObject = JsonParser.parseString(responseString).asJsonObject
                        val results = jsonObject.getAsJsonArray("results")
                        if (results.size() > 0) {
                            val result = results[0].asJsonObject
                            val formatted = result.get("formatted").asString
                            runOnUiThread {
                                foodLocation.text = formatted
                            }
                        } else {
                            runOnUiThread {
                                foodLocation.text = "No location found"
                            }
                        }
                    }
                }
            }
        })
    }

    private fun makeRequest(food: FoodNgo) {
        val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        val ngoId = sharedPreferences.getString("userid", null) ?: return

        val requestBody = JSONObject()
        requestBody.put("donationId", food.id)
        requestBody.put("ngoId", ngoId)

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5000/api/donation/donations/request")
            .put(RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), requestBody.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FoodDetailsActivity", "Request failed: ${e.message}")
                runOnUiThread {
                    // Handle the failure, e.g., show a Toast or update UI
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        // Handle the successful response, e.g., show a Toast or update UI
                    }
                } else {
                    Log.e("FoodDetailsActivity", "Request failed: ${response.message}")
                    runOnUiThread {
                        // Handle the failure, e.g., show a Toast or update UI
                    }
                }
            }
        })
    }
}
