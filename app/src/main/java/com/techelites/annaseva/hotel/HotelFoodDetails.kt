package com.techelites.annaseva.hotel

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.techelites.annaseva.auth.Donation
import com.techelites.annaseva.auth.Location
import com.techelites.annaseva.R
import okhttp3.*
import okhttp3.Request
import java.io.IOException

class HotelFoodDetails : AppCompatActivity() {

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
    private lateinit var foodDescription: TextView
    private lateinit var foodCreatedAt: TextView
    private lateinit var foodType: TextView
    private lateinit var foodLocation: TextView
    private lateinit var deleteButton: Button

    private val openCageApiKey = "3216512d44244bf1acd0fd1398aa2652"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hotel_food_details)

        // Initialize views
        foodName = findViewById(R.id.foodName)
        foodType = findViewById(R.id.foodType)
        foodCategory = findViewById(R.id.foodCategory)
        foodQuantity = findViewById(R.id.foodQuantity)
        foodExpiry = findViewById(R.id.foodExpiry)
        foodIdealFor = findViewById(R.id.foodIdealFor)
        foodAvailableAt = findViewById(R.id.foodAvailableAt)
        foodTransportation = findViewById(R.id.foodTransportation)
        foodContactPerson = findViewById(R.id.foodContactPerson)
        foodDonationStatus = findViewById(R.id.foodDonationStatus)
        foodPickupInstructions = findViewById(R.id.foodinstruction)
        foodDescription = findViewById(R.id.foodDescription)
        foodCreatedAt = findViewById(R.id.createdDate)
        foodLocation = findViewById(R.id.foodLocation)
        deleteButton = findViewById(R.id.deleteButton)

        // Handle back button click
        findViewById<ImageButton>(R.id.backArrowDetials).setOnClickListener {
            finish()
        }

        // Handle food item passed via intent
        // Handle food item passed via intent
        val foodItemJson = intent.getStringExtra("foodItem")
        Log.d("HotelFoodDetails", "Received foodItem JSON: $foodItemJson") // Debug log for JSON string
        foodItemJson?.let {
            val foodItem = Gson().fromJson(it, Donation::class.java)
            Log.d("HotelFoodDetails", "Received foodItem: $foodItem") // Debug log for parsed Donation object
            foodItem?.let { updateUI(it) }
        }


        // Handle delete button click
        deleteButton.setOnClickListener {
            if (foodItemJson != null) {
                deletePost(foodItemJson)
            }
        }
    }

    private fun updateUI(food: Donation) {
        println(food)
        Log.d("HotelFoodDetails", "Updating UI with food: $food")
        // Update UI with food details
        foodType.text = food.type
        foodName.text = food.name
        foodCategory.text = food.category
        foodQuantity.text = food.quantity.toString()
        foodExpiry.text = food.expiry
        foodIdealFor.text = food.idealfor
        foodAvailableAt.text = food.availableAt
        foodTransportation.text = food.transportation
        foodContactPerson.text = food.contactPerson
        foodDonationStatus.text = food.donationStatus
        foodPickupInstructions.text = food.pickupInstructions
        foodDescription.text = food.description
        foodCreatedAt.text = food.createdAt
        foodLocation.text = "Loading location..." // Placeholder until geocoding is done
        // Assuming the location is stored in a single string field
        food.location?.let { geocodeLocation(it) }
    }

    private fun geocodeLocation(location: Location) {
        val client = OkHttpClient()
        val lat = location.coordinates[0]
        val lon = location.coordinates[1]
        val request = Request.Builder()
            .url("https://api.opencagedata.com/geocode/v1/json?q=$lat,$lon&key=$openCageApiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HotelFoodDetails", "Geocoding failed: ${e.message}")
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

    private fun deletePost(food: String) {
        // Implement logic to delete post
        // For example, you can use Retrofit or OkHttp to send a request to your backend server
        // and handle the response here
    }
}
