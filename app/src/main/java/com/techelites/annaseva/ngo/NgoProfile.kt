package com.techelites.annaseva.ngo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.techelites.annaseva.R
import com.techelites.annaseva.auth.Start
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class NgoProfile : Fragment() {

    private lateinit var userName: TextView
    private lateinit var email: TextView
    private lateinit var logout: Button
    private lateinit var edit: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var setting: ImageButton

    private lateinit var totalDonations: TextView
    private lateinit var acceptedDonations: TextView
    private lateinit var deliveredDonations: TextView
    private lateinit var acceptPer: TextView
    private lateinit var deliveredPer: TextView

    private lateinit var userId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ngo_profile, container, false)
        edit = view.findViewById(R.id.editBtn)
        userName = view.findViewById(R.id.usernameP)
        email = view.findViewById(R.id.emailP)
        logout = view.findViewById(R.id.logoutbtn)
        progressBar = view.findViewById(R.id.progessId)
        progressBar.visibility = View.INVISIBLE
        setting = view.findViewById(R.id.settings)

        totalDonations = view.findViewById(R.id.foundcount)
        acceptedDonations = view.findViewById(R.id.foundcountt)
        deliveredDonations = view.findViewById(R.id.lostcount)
        acceptPer = view.findViewById(R.id.acceptPer)
        deliveredPer = view.findViewById(R.id.deliverPer)

        val pref: SharedPreferences = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE)
        val savedUserName = pref.getString("userName", "")
        val savedEmail = pref.getString("email", "")
        userId = pref.getString("userId", "").toString()

        userName.text = savedUserName
        email.text = savedEmail

        fetchAnalyticsData(userId)

        logout.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                val editor: SharedPreferences.Editor = pref.edit()
                editor.putBoolean("flag", false)
                editor.apply()
                progressBar.visibility = View.GONE
                val intentStart = Intent(requireContext(), Start::class.java)
                startActivity(intentStart)
            }, 2000)
        }

        edit.setOnClickListener {
            // Handle edit action
        }

        return view
    }

    private fun fetchAnalyticsData(userId: String) {
        val client = OkHttpClient()
        val url = "https://anna-seva-backend.onrender.com/ngo/analytics/$userId"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val jsonObject = JSONObject(responseData)
                        val data = jsonObject.getJSONObject("data")

                        val total = data.getLong("totalDonations")
                        val accepted = data.getLong("acceptedDonations")
                        val delivered = data.getLong("deliveredDonations")
                        val acceptedPercentage = data.getDouble("acceptedPercentage")
                        val deliveredPercentage = data.getDouble("deliveredPercentage")

                        requireActivity().runOnUiThread {
                            totalDonations.text = total.toString()
                            acceptedDonations.text = accepted.toString()
                            deliveredDonations.text = delivered.toString()
                            acceptPer.text = String.format("%.2f%%", acceptedPercentage)
                            deliveredPer.text = String.format("%.2f%%", deliveredPercentage)
                        }
                    }
                } else {
                    // Handle error response
                }
            }
        })
    }
}
