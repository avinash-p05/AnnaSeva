package com.techelites.annaseva.hotel

import FoodHotel
import FoodNgo
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
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.techelites.annaseva.R
import com.techelites.annaseva.auth.Donation
import com.techelites.annaseva.common.FoodDetailsActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class AcceptedRequestsListings : Fragment(),AcceptedFoodAdapterHotel.OnRecipeClickListener {
    private lateinit var userId: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: AcceptedFoodAdapterHotel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hotel_dashboard, container, false)

        recyclerView = view.findViewById(R.id.recipeViewH2)
        progressBar = view.findViewById(R.id.progressBarH2)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AcceptedFoodAdapterHotel(ArrayList(),requireContext(),this)
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
            .url("https://anna-seva-backend.onrender.com/hotel/acceptedDonations/$userId")
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

    private fun parseDonationsJson(jsonString: String?): List<FoodHotel> {
        val donationsList = mutableListOf<FoodHotel>()
        try {
            val jsonObject = JSONObject(jsonString)
            if (jsonObject.getBoolean("success")) {
                val dataArray = jsonObject.getJSONArray("data")
                for (i in 0 until dataArray.length()) {
                    val donationObject = dataArray.getJSONObject(i)
                    val donation = Gson().fromJson(donationObject.toString(), FoodHotel::class.java)
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

    override fun onRecipeClick(food: FoodHotel) {
        // Handle the click event, navigate to FoodDetailsActivity
        val intent = Intent(context, NotificationDetailsActivity::class.java)
        // Pass necessary data to FoodDetailsActivity using Intent extras
        intent.putExtra("foodItem", food)
        startActivity(intent)
    }
}