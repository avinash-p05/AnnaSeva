package com.techelites.annaseva.volunteer

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.JsonParser
import com.techelites.annaseva.R
import FoodNgo
import android.animation.Animator
import android.widget.ImageView
import android.widget.Toast
import com.squareup.picasso.Picasso
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class FoodDetailsActivity : AppCompatActivity() {

    private lateinit var foodName: TextView
    private lateinit var foodImage: ImageView
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
    private lateinit var foodType: TextView
    private lateinit var foodLocation: TextView
    private lateinit var requestButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var lottieAnimationView: LottieAnimationView

    private val openCageApiKey = "3216512d44244bf1acd0fd1398aa2652"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_details)

        foodName = findViewById(R.id.foodName)
        foodImage = findViewById(R.id.imageViewD)
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
        foodLocation = findViewById(R.id.foodLocation)
        requestButton = findViewById(R.id.requestButton)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility= View.GONE

        lottieAnimationView = findViewById(R.id.lottieAnimationView)

        val foodItem = intent.getParcelableExtra<FoodNgo>("foodItem")
        foodItem?.let {
            updateUI(it)
            checkRequestStatus(it) // Check if the NGO has already requested the donation
        }

        requestButton.setOnClickListener {
            foodItem?.let { showConfirmDialog(it) }
        }

        findViewById<ImageButton>(R.id.backArrowDetials).setOnClickListener {
            finish()
        }
    }

    private fun updateUI(food: FoodNgo) {
        val imageUrl = "http://annaseva.ajinkyatechnologies.in/${food.uploadPhoto}"
        Picasso.get().load(imageUrl).into(foodImage)
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
        foodHotelName.text = food.hotel.name
        foodDescription.text = food.description
        foodLocation.text = "Loading location..." // Placeholder until geocoding is done
        food.hotel.location.coordinates?.let { geocodeLocation(it.toString()) }
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
                Log.d("FoodDetailsActivity", "Response received: $response")
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->

                        Log.d("FoodDetailsActivity", "Response body received: $responseBody")
                        val responseString = responseBody.string()
                        Log.d("FoodDetailsActivity", "Response string: $responseString")
                        val jsonObject = JsonParser.parseString(responseString).asJsonObject
                        Log.d("FoodDetailsActivity", "JSON object: $jsonObject")
                        val results = jsonObject.getAsJsonArray("results")
                        Log.d("FoodDetailsActivity", "Results array: $results")
                        if (results != null && results.size() > 0) { // Check if results is not null
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

    private fun checkRequestStatus(food: FoodNgo) {
        val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        val ngoId = sharedPreferences.getString("userid", null) ?: return

        val requestId = sharedPreferences.getString("request_${food.id}", null)
        requestId?.let { // If requestId is not null
            // The NGO has already requested this donation
            runOnUiThread {
                // Update button to requested state
                requestButton.text = "Requested"
                requestButton.setBackgroundColor(resources.getColor(R.color.main, null))
                requestButton.setTextColor(Color.WHITE)
                requestButton.isEnabled = false
            }
        } ?: run {
            // The NGO has not requested this donation yet
            runOnUiThread {
                // Update button to request state
                requestButton.text = "Request Donation"
                requestButton.setBackgroundColor(resources.getColor(R.color.main, null))
                requestButton.isEnabled = true
            }
        }
    }

    private fun showConfirmDialog(food: FoodNgo) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Request")
        builder.setMessage("Are you sure you want to request this donation?")
        builder.setPositiveButton("Confirm") { _, _ ->
            startProgress()
            food.let { makeRequest(it) }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun startProgress() {
        progressBar.visibility= View.GONE
        requestButton.isEnabled = false
        progressBar.visibility = View.VISIBLE

        // Play the animation
        lottieAnimationView.visibility = View.VISIBLE
        lottieAnimationView.playAnimation()

        // Change button text and color
        requestButton.text = "Requested"
        requestButton.setBackgroundColor(resources.getColor(R.color.main, null))
        requestButton.setTextColor(Color.WHITE)

        // Make animation view invisible after playing
        // Inside startProgress() function after playing the animation
        lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                lottieAnimationView.visibility = View.GONE
                lottieAnimationView.removeAnimatorListener(this)
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
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
            .url("http://annaseva.ajinkyatechnologies.in/api/donation/donations/request")
            .put(RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), requestBody.toString()))

            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FoodDetailsActivity", "Request failed: ${e.message}")
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    // Handle the failure, e.g., show a Toast or update UI
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val responseString = responseBody.string()
                        val jsonObject = JsonParser.parseString(responseString).asJsonObject
                        if (jsonObject.get("success").asBoolean) {
                            runOnUiThread {
                                // Store the request ID and map it to the donation ID
                                val requestId = jsonObject.getAsJsonObject("request").getAsJsonPrimitive("_id").asString
                                // Store the requestId in your SharedPreferences or database along with the donationId
                                // For example, you can use SharedPreferences to store the mapping
                                val sharedPrefsEditor = sharedPreferences.edit()
                                sharedPrefsEditor.putString("request_${food.id}", requestId)
                                sharedPrefsEditor.apply()

                                // Show toast indicating success
                                Toast.makeText(this@FoodDetailsActivity, "Requested successfully", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            runOnUiThread {
                                progressBar.visibility = View.GONE
                                // Handle the failure, e.g., show a Toast or update UI
                                // You can show a Toast message or an alert dialog to notify the user about the failure
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        // Handle the failure, e.g., show a Toast or update UI
                        // You can show a Toast message or an alert dialog to notify the user about the failure
                    }
                }
            }
        })
    }

}

