package com.techelites.annaseva.ngo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.techelites.annaseva.R
import com.techelites.annaseva.common.FoodDetailsActivity
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NgoFoodListings : Fragment(), FoodAdapterNgo.OnRecipeClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: FoodAdapterNgo
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ngo_food_listings, container, false)

        recyclerView = view.findViewById(R.id.recipeView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recipeAdapter = FoodAdapterNgo(ArrayList(), requireContext(), this)
        recyclerView.adapter = recipeAdapter

        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        // Load recipes from API
        loadRecipes()

        return view
    }

    // Function to load recipes from API
    private fun loadRecipes() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5000/api/donation/donations") // Replace with your actual API URL
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

                        val foodList = parseFoodList(responseString)

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

    private fun parseFoodList(responseString: String): List<FoodNgo> {
        val foodList = ArrayList<FoodNgo>()

        try {
            val jsonObject = JSONObject(responseString)
            val donationsArray = jsonObject.optJSONArray("donations")

            donationsArray?.let {
                for (i in 0 until it.length()) {
                    val donation = it.getJSONObject(i)
                    val status = donation.optJSONArray("requests")

                    if (!status.equals("Available")) {
                        val locationObject = donation.optJSONObject("location")
                        val coordinates = locationObject?.optJSONArray("coordinates")
                        val locationString = if (coordinates != null) {
                            "${coordinates.optDouble(1, 0.0)},${coordinates.optDouble(0, 0.0)}"
                        } else {
                            "0.0,0.0"
                        }

                        val hotelId = donation.optJSONObject("hotel")?.optString("_id", null)
                        val hotelName = donation.optJSONObject("hotel")?.optString("name", null) // Get hotel name based on hotelId
                        var dateEp = donation.optString("expiry", null)
                        dateEp = formatDateString(dateEp)
                        var dateAb = donation.optString("availableAt", null)
                        dateAb = formatDateString(dateAb)
                        var dateCr = donation.optString("createdAt", null)
                        dateCr = formatDateString(dateCr)

                        val foodItem = FoodNgo(
                            id = donation.optString("_id", null),
                            type = donation.optString("type", null),
                            name = donation.optString("name", null),
                            description = donation.optString("description", null),
                            category = donation.optString("category", null),
                            quantity = donation.optInt("quantity", 0),
                            expiry = dateEp,
                            idealfor = donation.optString("idealfor", null),
                            availableAt = dateAb,
                            location = locationString,
                            transportation = donation.optString("transportation", null),
                            contactPerson = donation.optString("contactPerson", null),
                            donationStatus = donation.optString("donationStatus", null),
                            pickupInstructions = donation.optString("pickupInstructions", null),
                            hotelId = hotelId,
                            hotelName = hotelName,
                            isUsable = donation.optBoolean("isUsable", false),
                            autoAssignStatus = donation.optString("autoAssignStatus", null),
                            createdAt = dateCr,
                            updatedAt = donation.optString("updatedAt", null)
                        )
                        foodList.add(foodItem)
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return foodList
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
