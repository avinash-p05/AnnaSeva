
package com.techelites.annaseva.ngo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonParser
import com.google.zxing.integration.android.IntentIntegrator
import com.squareup.picasso.Picasso
import com.techelites.annaseva.Notification
import com.techelites.annaseva.R
import com.techelites.annaseva.databinding.ActivityNgoNotificationsDetailsBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class NgoNotificationsDetails : AppCompatActivity() {
    private lateinit var userId: String
    private lateinit var binding: ActivityNgoNotificationsDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNgoNotificationsDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve userId from SharedPreferences
        val pref = getSharedPreferences("login", Context.MODE_PRIVATE)
        userId = pref.getString("userid", "").toString()

        val notification = intent.getSerializableExtra("notification") as? Notification
        if (notification == null) {
            Toast.makeText(this, "Notification data is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Check if the notification message contains "New Donation added"
        if (notification.message.contains("New Donation available", ignoreCase = true)) {
            binding.btnTrackOrder.visibility = View.GONE
        }

        val donationId = notification.metadata.donationId
        Log.d("NgoNotificationsDetails", "Donation ID: $donationId")

        if (donationId.isNotEmpty()) {
            fetchDonationDetails(donationId)
        } else {
            Toast.makeText(this, "Invalid donation ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnTrackOrder.setOnClickListener {
            fetchTrackingDetails(donationId)
        }
    }

    private fun fetchDonationDetails(donationId: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.1.6:8080/donation/getDonationById?id=$donationId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("NgoNotificationsDetails", "Failed to fetch donation details: ${e.message}")
                    Toast.makeText(this@NgoNotificationsDetails, "Failed to load donation details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        try {
                            val responseString = responseBody.string()
                            val jsonObject = JsonParser.parseString(responseString).asJsonObject
                            val donationJson = jsonObject.getAsJsonObject("donation")

                            runOnUiThread {
                                val imageUrl = "http://annaseva.ajinkyatechnologies.in/${donationJson.get("uploadPhoto")?.asString}"
                                Picasso.get().load(imageUrl).into(binding.imageViewD)
                                binding.tvDonationName.text = donationJson.get("name")?.asString ?: ""
                                binding.tvDonationDescription.text = donationJson.get("description")?.asString ?: ""
                                binding.tvDonationCategory.text = donationJson.get("category")?.asString ?: ""
                                binding.tvDonationQuantity.text = donationJson.get("quantity")?.asInt?.toString() ?: ""
                                binding.tvDonationExpiry.text = donationJson.get("expiry")?.asString ?: ""
                                binding.tvDonationIdealFor.text = donationJson.get("idealfor")?.asString ?: ""
                                binding.tvDonationPickupInstructions.text = donationJson.get("pickupInstructions")?.asString ?: ""
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Log.e("NgoNotificationsDetails", "Error parsing donation details: ${e.message}")
                                Toast.makeText(this@NgoNotificationsDetails, "Error parsing donation details", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        Log.e("NgoNotificationsDetails", "Failed to fetch donation details: ${response.code} - ${response.message}")
                        Toast.makeText(this@NgoNotificationsDetails, "Failed to load donation details", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun fetchTrackingDetails(donationId: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://annaseva.ajinkyatechnologies.in/api/tracking/tracking/donation?id=$donationId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("NgoNotificationsDetails", "Failed to fetch tracking details: ${e.message}")
                    Toast.makeText(this@NgoNotificationsDetails, "Failed to load tracking details", Toast.LENGTH_SHORT).show()
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
                                    val trackingRecords = jsonObject.getAsJsonArray("trackingRecords")
                                    if (trackingRecords.size() > 0) {
                                        val status = trackingRecords[0].asJsonObject.get("status")?.asString ?: ""
                                        showStatusDialog(status)
                                    } else {
                                        Toast.makeText(this@NgoNotificationsDetails, "Tracking records not found", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this@NgoNotificationsDetails, "Failed to fetch tracking details", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Log.e("NgoNotificationsDetails", "Error parsing tracking details: ${e.message}")
                                Toast.makeText(this@NgoNotificationsDetails, "Error parsing tracking details", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        Log.e("NgoNotificationsDetails", "Failed to fetch tracking details: ${response.code} - ${response.message}")
                        Toast.makeText(this@NgoNotificationsDetails, "Failed to load tracking details", Toast.LENGTH_SHORT).show()
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
        val jsonBody = """
            {
                "ngoId": "$userId",
                "location": "Mahesh Foundation",
                "percentageReached": "100"
            }
        """.trimIndent()
        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), jsonBody)
        val request = Request.Builder()
            .url("http://annaseva.ajinkyatechnologies.in/api/tracking/scan/ngo?id=$trackingId")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("NgoNotificationsDetails", "Failed to update status: ${e.message}")
                    Toast.makeText(this@NgoNotificationsDetails, "Failed to update status", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@NgoNotificationsDetails, "Failed to update status", Toast.LENGTH_SHORT).show()
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
}
