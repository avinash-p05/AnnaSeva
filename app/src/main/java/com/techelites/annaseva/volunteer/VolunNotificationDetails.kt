package com.techelites.annaseva.volunteer

import FoodNgo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.JsonParser
import com.google.zxing.integration.android.IntentIntegrator
import com.squareup.picasso.Picasso
import com.techelites.annaseva.Notification
import com.techelites.annaseva.R
import com.techelites.annaseva.databinding.ActivityFoodDetailsVolunteerBinding
import com.techelites.annaseva.databinding.ActivityVolunNotificationDetailsBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class VolunNotificationDetails : AppCompatActivity() {
    private lateinit var userId: String
    private lateinit var binding: ActivityVolunNotificationDetailsBinding

    private lateinit var foodName: TextView
    private lateinit var foodImage: ImageView
    private lateinit var foodCategory: TextView
    private lateinit var foodQuantity: TextView
    private lateinit var foodExpiry: TextView
    private lateinit var foodIdealFor: TextView
    private lateinit var foodPickupInstructions: TextView
    private lateinit var foodDescription: TextView
    private lateinit var foodType: TextView
    private lateinit var trackButton: Button
    private val openCageApiKey = "3216512d44244bf1acd0fd1398aa2652"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVolunNotificationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve userId from SharedPreferences
        val pref = getSharedPreferences("login", Context.MODE_PRIVATE)
        userId = pref.getString("userId", "").toString()

        foodName = findViewById(R.id.tvDonationName)
        foodImage = findViewById(R.id.imageViewD)
        foodType = findViewById(R.id.foodType)
        foodCategory = findViewById(R.id.tvDonationCategory)
        foodQuantity = findViewById(R.id.tvDonationQuantity)
        foodExpiry = findViewById(R.id.tvDonationExpiry)
        foodIdealFor = findViewById(R.id.tvDonationIdealFor)
        foodPickupInstructions = findViewById(R.id.tvDonationPickupInstructions)
        foodDescription = findViewById(R.id.tvDonationDescription)
        trackButton = findViewById(R.id.btnTrackOrder)


        // Check if the notification message contains "New Donation added"

        val foodItem = intent.getParcelableExtra<FoodNgo>("foodItem")
        foodItem?.let {
            updateUI(it)
        }

        binding.btnTrackOrder.setOnClickListener {
            if (foodItem != null) {
                fetchTrackingDetails(foodItem.id)
            }
        }
    }


    private fun fetchTrackingDetails(donationId: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://anna-seva-backend.onrender.com/tracking/status/$donationId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("VolunNotificationDetails", "Failed to fetch tracking details: ${e.message}")
                    Toast.makeText(this@VolunNotificationDetails, "Failed to load tracking details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        try {
                            val responseString = responseBody.string()
                            val jsonObject = JsonParser.parseString(responseString).asJsonObject
                            val success = jsonObject.get("success")?.asBoolean ?: false

                            runOnUiThread {
                                if (success) {
                                    val status = jsonObject.get("message")?.asString ?: ""
                                    showStatusDialog(status)
                                } else {
                                    Toast.makeText(this@VolunNotificationDetails, "Failed to fetch tracking details", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Log.e("VolunNotificationDetails", "Error parsing tracking details: ${e.message}")
                                Toast.makeText(this@VolunNotificationDetails, "Error parsing tracking details", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        Log.e("VolunNotificationDetails", "Failed to fetch tracking details: ${response.code} - ${response.message}")
                        Toast.makeText(this@VolunNotificationDetails, "Failed to load tracking details", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    private fun showStatusDialog(status: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Tracking Status")
        dialogBuilder.setMessage("Status: $status")
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        // Only add the "Update Status" button if the status is "Shipped"
        if (status.equals("Shipped", ignoreCase = true)) {
            dialogBuilder.setNegativeButton("Update Status") { dialog, _ ->
                initiateQrScan()
            }
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun initiateQrScan() {
        IntentIntegrator(this).initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                updateStatusToDelivered(result.contents)
            } else {
                Toast.makeText(this, "QR Code scan cancelled", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun updateStatusToDelivered(trackingId: String) {
        val client = OkHttpClient()

        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), "{}")
        val request = Request.Builder()
            .url("https://anna-seva-backend.onrender.com/tracking/ngo/$trackingId")
            .put(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("NgoNotificationsDetails", "Failed to update status: ${e.message}")
                    Toast.makeText(this@VolunNotificationDetails, "Failed to update status", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        val message = JsonParser.parseString(responseBody).asJsonObject.get("message").asString
                        showConfirmationDialog(message)
                    } else {
                        Log.e("NgoNotificationsDetails", "Failed to update status: ${response.code} - ${response.message}")
                        Toast.makeText(this@VolunNotificationDetails, "Failed to update status", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun showConfirmationDialog(message: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Status Update")
        dialogBuilder.setMessage("Delivered!!")
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }

    private fun updateUI(food: FoodNgo) {
        val imageUrl = food.imageUrl
        Picasso.get().load(imageUrl).into(foodImage)
        // Update UI with food details
        foodType.text = food.type
        foodName.text = food.name
        foodCategory.text = food.category
        foodQuantity.text = food.quantity.toString()
        foodExpiry.text = food.expiry.toString()
        foodIdealFor.text = food.idealFor
        foodDescription.text = food.description
        foodPickupInstructions.text = food.pickupInstructions

    }
}