package com.techelites.annaseva.hotel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.techelites.annaseva.DonationsResponse
import com.techelites.annaseva.R
import okhttp3.*
import java.io.IOException

class HotelFoodListings : Fragment() {
    private lateinit var userId: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: FoodAdapterHotel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hotel_food_listings, container, false)

        recyclerView = view.findViewById(R.id.recipeView)
        progressBar = view.findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FoodAdapterHotel(requireContext(), mutableListOf()) { donation ->
            // Handle item click
            val intent = Intent(requireContext(), HotelFoodDetails::class.java)
            intent.putExtra("foodItem", Gson().toJson(donation))
            Log.d("SendingActivity", "Sending foodItem: $donation")

            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Load donations when fragment is created
        loadDonations()

        return view
    }

    private fun loadDonations() {
        progressBar.visibility = View.VISIBLE // Show progress bar
        val pref: SharedPreferences = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE)
        userId = pref.getString("userid", "").toString()
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:5000/api/donation/getallrequests/$userId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gson = Gson()
                val donationsResponse = gson.fromJson(body, DonationsResponse::class.java)

                // Update UI on the main thread
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE // Hide progress bar

                    if (donationsResponse != null && donationsResponse.success) {
                        if (donationsResponse.donations.isNotEmpty()) {
                            adapter.updateList(donationsResponse.donations) // Update adapter with donations list
                            recyclerView.visibility = View.VISIBLE // Show recycler view
                        } else {
                            recyclerView.visibility = View.GONE // Hide recycler view
                        }
                    } else {
                        recyclerView.visibility = View.GONE // Hide recycler view
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Handle network request failure
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE // Hide progress bar
                    recyclerView.visibility = View.GONE // Hide recycler view
                }
            }
        })
    }
}
