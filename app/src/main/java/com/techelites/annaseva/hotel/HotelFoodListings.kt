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
import com.techelites.annaseva.auth.Donation
import com.techelites.annaseva.R
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
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
        adapter = FoodAdapterHotel(requireContext(), mutableListOf(), { donation ->
            // Handle view details click
            val intent = Intent(requireContext(), HotelFoodDetails::class.java)
            intent.putExtra("foodItem", Gson().toJson(donation))
            Log.d("SendingActivity", "Sending foodItem: $donation")
            startActivity(intent)
        }, { donation ->
            // Handle view requests click
            showRequestsDialog(donation)
        })
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
            .url("http://annaseva.ajinkyatechnologies.in/api/donation/donationsbyhotel?id=$userId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val donations = parseDonationsJson(body)

                // Update UI on the main thread
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE // Hide progress bar

                    if (donations.isNotEmpty()) {
                        adapter.updateList(donations) // Update adapter with donations list
                        recyclerView.visibility = View.VISIBLE // Show recycler view
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

    private fun parseDonationsJson(jsonString: String?): List<Donation> {
        val donationsList = mutableListOf<Donation>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val donation = Gson().fromJson(jsonObject.toString(), Donation::class.java)
                donationsList.add(donation)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return donationsList
    }

    private fun showRequestsDialog(donation: Donation) {
        // Implement the logic to show a dialog with the list of requests for the donation
        // For now, you can show a placeholder dialog
        val dialog = RequestsDialogFragment.newInstance(donation._id)
        dialog.show(childFragmentManager, "RequestsDialog")
    }
}
