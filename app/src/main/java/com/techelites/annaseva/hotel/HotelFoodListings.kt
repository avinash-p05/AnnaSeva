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
import com.techelites.annaseva.R
import com.techelites.annaseva.auth.Donation
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
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
            // Handle view details click
            val intent = Intent(requireContext(), HotelFoodDetails::class.java)
            intent.putExtra("foodItem", Gson().toJson(donation))
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Load donations when fragment is created
        loadDonations()

        return view
    }

    private fun loadDonations() {
        progressBar.visibility = View.VISIBLE
        val pref: SharedPreferences = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE)
        userId = pref.getString("userId", "").toString()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://anna-seva-backend.onrender.com/hotel/availableDonations/$userId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val donations = parseDonationsJson(it)
                    requireActivity().runOnUiThread {
                        progressBar.visibility = View.GONE
                        adapter.updateList(donations)
                        recyclerView.visibility = if (donations.isNotEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("HotelFoodListings", "Network request failed", e)
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                }
            }
        })
    }

    private fun parseDonationsJson(jsonString: String?): List<Donation> {
        val donationsList = mutableListOf<Donation>()
        try {
            val jsonObject = JSONObject(jsonString)
            if (jsonObject.getBoolean("success")) {
                val dataArray = jsonObject.getJSONArray("data")
                for (i in 0 until dataArray.length()) {
                    val donationObject = dataArray.getJSONObject(i)
                    val donation = Gson().fromJson(donationObject.toString(), Donation::class.java)
                    donationsList.add(donation)
                }
            } else {
                Log.e("HotelFoodListings", "Failed to retrieve donations: ${jsonObject.getString("message")}")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return donationsList.reversed()
    }

    private fun showRequestsDialog(donationId: String) {
        val dialog = RequestsDialogFragment.newInstance(donationId)
        dialog.show(childFragmentManager, "RequestsDialog")
    }
}
