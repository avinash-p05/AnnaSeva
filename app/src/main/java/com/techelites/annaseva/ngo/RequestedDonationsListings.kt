package com.techelites.annaseva.ngo

import FoodNgo
import HotelNgo
import LocationNgo
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.techelites.annaseva.R
import com.techelites.annaseva.common.FoodDetailsActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RequestedDonationsListings : Fragment(),RequestedFoodAdapterNgo.OnRecipeClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RequestedFoodAdapterNgo
    private lateinit var progressBar: ProgressBar
    private lateinit var  userId : String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ngo_dashboard, container, false)

        recyclerView = view.findViewById(R.id.recipeViewN2)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recipeAdapter = RequestedFoodAdapterNgo(ArrayList(), requireContext(), this)
        recyclerView.adapter = recipeAdapter

        progressBar = view.findViewById(R.id.progressBarN2)
        progressBar.visibility = View.VISIBLE

        // Load recipes from API
        loadRecipes()

        return view
    }

    // Function to load recipes from API
    private fun loadRecipes() {
        val pref: SharedPreferences = requireActivity().getSharedPreferences("login",
            Context.MODE_PRIVATE
        )
        userId = pref.getString("userId","").toString()
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://anna-seva-backend.onrender.com/ngo/requestedDonations/${userId}") // Replace with your actual API URL
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to fetch data: ${e.message}")
                activity?.runOnUiThread {
                    progressBar.visibility = View.GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.use { responseBody ->
                        val responseString = responseBody.string()
                        Log.d(TAG, "Response: $responseString")

                        val foodList = parseJsonResponse(responseString)

                        activity?.runOnUiThread {
                            recipeAdapter.updateRecipes(foodList)
                            progressBar.visibility = View.GONE
                        }
                    }
                } else {
                    Log.e(TAG, "Unexpected response: ${response.message}")
                    activity?.runOnUiThread {
                        progressBar.visibility = View.GONE
                    }
                }
            }
        })
    }
    fun parseJsonResponse(response: String): List<FoodNgo> {
        val jsonObject = JSONObject(response)
        val donationsArray = jsonObject.getJSONArray("data")
        val donationsList = mutableListOf<FoodNgo>()

        for (i in 0 until donationsArray.length()) {
            val donationObject = donationsArray.getJSONObject(i)

            // Parse the location
            val locationObject = donationObject.getJSONObject("location")
            val location = LocationNgo(
                locationObject.getString("type"),
                doubleArrayOf(
                    locationObject.getJSONArray("coordinates").getDouble(0),
                    locationObject.getJSONArray("coordinates").getDouble(1)
                )
            )

            // Parse the hotel information
            val hotelObject = donationObject.getJSONObject("hotel")
            val hotelLocationObject = hotelObject.getJSONObject("location")
            val hotelLocation = LocationNgo(
                hotelLocationObject.getString("type"),
                doubleArrayOf(
                    hotelLocationObject.getJSONArray("coordinates").getDouble(0),
                    hotelLocationObject.getJSONArray("coordinates").getDouble(1)
                )
            )

            val hotel = HotelNgo(
                hotelObject.getString("id"),
                hotelObject.getString("name"),
                hotelObject.getString("email"),
                hotelObject.optString("password", ""),
                hotelObject.getString("address"),
                hotelObject.getString("city"),
                hotelObject.getString("state"),
                hotelObject.getString("pinCode"),
                hotelLocation,
                hotelObject.getString("contactPerson"),
                hotelObject.getString("contactNumber"),
                hotelObject.getJSONArray("donations").let { donationsJsonArray ->
                    List(donationsJsonArray.length()) { donationsJsonArray.getString(it) }
                },
                hotelObject.getString("createdAt"),
                hotelObject.getString("updatedAt"),
                hotelObject.optBoolean("verified")
            )

            // Parse the donation details
            val foodNgo = FoodNgo(
                donationObject.getString("id"),
                donationObject.getString("type"),
                donationObject.getString("name"),
                donationObject.getString("description"),
                donationObject.getString("category"),
                donationObject.getInt("quantity"),
                donationObject.getString("expiry"),
                donationObject.getString("idealFor"),
                donationObject.optString("availableAt", ""),
                location,
                donationObject.getString("transportation"),
                donationObject.optString("imageUrl"),
                donationObject.getJSONObject("requests").keys().asSequence().toList(),
                donationObject.getString("contactPerson"),
                donationObject.getString("donationStatus"),
                donationObject.getString("pickupInstructions"),
                hotel,
                donationObject.optString("autoAssignStatus", ""),
                donationObject.optString("shipmentStatus", ""),
                donationObject.optBoolean("hotelCoversTransport"),
                donationObject.getString("createdAt"),
                donationObject.getString("updatedAt")
            )

            donationsList.add(foodNgo)
        }

        return donationsList.reversed()
    }

    override fun onRecipeClick(food: FoodNgo) {
        // Handle the click event, navigate to FoodDetailsActivity
        val intent = Intent(context, FoodDetailsActivity::class.java)
        // Pass necessary data to FoodDetailsActivity using Intent extras
        intent.putExtra("foodItem", food)
        startActivity(intent)
    }

    private fun formatDateString(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val date: Date = inputFormat.parse(dateString) ?: return dateString
        return outputFormat.format(date)
    }

    companion object {
        private const val TAG = "NgoFoodListings"
    }
}