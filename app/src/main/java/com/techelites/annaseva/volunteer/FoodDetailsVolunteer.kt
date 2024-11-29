package com.techelites.annaseva.volunteer

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
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

class FoodDetailsVolunteer : AppCompatActivity() {

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
        setContentView(R.layout.activity_food_details_volunteer)

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
        val imageUrl = "http://annaseva.ajinkyatechnologies.in/${food.imageUrl}"
        Picasso.get().load(imageUrl).into(foodImage)
        // Update UI with food details
        foodType.text = food.type
        foodName.text = food.name
        foodCategory.text = food.category
        foodQuantity.text = food.quantity.toString()
        foodExpiry.text = food.expiry.toString()
        foodIdealFor.text = food.idealFor
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
                        val jsonObject = JsonParser.parseString(responseString).asJsonObject
                        val resultsArray = jsonObject.getAsJsonArray("results")
                        if (resultsArray.size() > 0) {
                            val firstResult = resultsArray.get(0).asJsonObject
                            val formattedLocation = firstResult.get("formatted").asString
                            runOnUiThread {
                                foodLocation.text = formattedLocation
                            }
                        } else {
                            runOnUiThread {
                                foodLocation.text = "No results found"
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        foodLocation.text = "Geocoding failed"
                    }
                }
            }
        })
    }

    private fun showConfirmDialog(food: FoodNgo) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Confirm Request")
        dialogBuilder.setMessage("Are you sure you want to request this food donation?")
        dialogBuilder.setPositiveButton("Yes") { dialog, _ ->
            startProgress()
            makeRequest(food)
            dialog.dismiss()
        }
        dialogBuilder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun makeRequest(food: FoodNgo) {
        val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        val volunteerId = sharedPreferences.getString("userid", null) ?: return

        val requestBody = JSONObject()
        requestBody.put("donationId", food.id)
        requestBody.put("volunteerId", volunteerId)

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://annaseva.ajinkyatechnologies.in/api/donation/accept-donation-by-volunteer")
            .post(RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), requestBody.toString()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FoodDetailsActivity", "Request failed: ${e.message}")
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@FoodDetailsVolunteer, "Request failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                }

                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val responseString = responseBody.string()
                        val jsonObject = JsonParser.parseString(responseString).asJsonObject

                        Log.d("FoodDetailsActivity", "Response: $jsonObject")

                        if (jsonObject.has("success") && jsonObject.get("success").asBoolean) {
                            runOnUiThread {
                                // Assuming there's no request ID in the response, just show a success message
                                Toast.makeText(this@FoodDetailsVolunteer, "Claimed successfully", Toast.LENGTH_SHORT).show()

                                // Optionally, you can save some other information if available
                                // val message = jsonObject.getAsJsonPrimitive("message")?.asString
                                // if (message != null) {
                                //     sharedPreferences.edit().putString("lastSuccessMessage", message).apply()
                                // }
                            }
                        } else {
                            runOnUiThread {
                                Log.e("FoodDetailsActivity", "Success flag not found or false in response")
                                Toast.makeText(this@FoodDetailsVolunteer, "Request failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } ?: run {
                        runOnUiThread {
                            Log.e("FoodDetailsActivity", "Response body is null")
                            Toast.makeText(this@FoodDetailsVolunteer, "Response body is null", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Log.e("FoodDetailsActivity", "Response is not successful")
                        Toast.makeText(this@FoodDetailsVolunteer, "Request failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }



    private fun startProgress() {
        progressBar.visibility = View.VISIBLE
        lottieAnimationView.visibility = View.VISIBLE
        lottieAnimationView.playAnimation()
        lottieAnimationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                lottieAnimationView.visibility = View.GONE
                progressBar.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationRepeat(animation: Animator) {
            }
        })
    }

    private fun checkRequestStatus(food: FoodNgo) {
        val sharedPreferences = getSharedPreferences("login", MODE_PRIVATE)
        val requestId = sharedPreferences.getString("request_${food.id}", null)
        if (requestId != null) {
            // If requestId exists, the donation has already been requested
            requestButton.isEnabled = false
            requestButton.text = "Request Already Made"
            requestButton.setBackgroundColor(Color.GRAY)
        }
    }
}
