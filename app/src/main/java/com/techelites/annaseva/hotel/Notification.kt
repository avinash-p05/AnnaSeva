package com.techelites.annaseva.hotel

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser
import com.techelites.annaseva.AppNotification
import com.techelites.annaseva.NotificationMetadata
import com.techelites.annaseva.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class Notification : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        recyclerView = view.findViewById(R.id.recipeViewT)
        progressBar = view.findViewById(R.id.progressBarT)
        recyclerView.layoutManager = LinearLayoutManager(context)
        loadNotifications()
        return view
    }

    private fun loadNotifications() {
        progressBar.visibility = View.VISIBLE
        val sharedPreferences =requireActivity().getSharedPreferences("login",
            Context.MODE_PRIVATE
        )
        val userId = sharedPreferences.getString("userid", null) ?: return

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://annaseva.ajinkyatechnologies.in/api/donation/notifications/$userId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Failed to load notifications", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val responseString = responseBody.string()
                        val jsonObject = JsonParser.parseString(responseString).asJsonObject
                        val success = jsonObject.get("success")?.asBoolean ?: false
                        if (success) {
                            val notificationsJsonArray = jsonObject.getAsJsonArray("notifications")
                            val notifications = notificationsJsonArray.map { it.asJsonObject }.map {
                                AppNotification(
                                    _id = it.get("_id")?.asString ?: "",
                                    recipient = it.get("recipient")?.asString ?: "",
                                    message = it.get("message")?.asString ?: "",
                                    type = it.get("type")?.asString ?: "",
                                    metadata = NotificationMetadata(
                                        donationId = it.getAsJsonObject("metadata")?.get("donationId")?.asString ?: "",
                                        trackingId = it.getAsJsonObject("metadata")?.get("trackingId")?.asString ?: ""
                                    ),
                                    read = it.get("read")?.asBoolean ?: false,
                                    createdAt = it.get("createdAt")?.asString ?: "",
                                    __v = it.get("__v")?.asInt ?: 0
                                )
                            }

                            requireActivity().runOnUiThread {
                                progressBar.visibility = View.GONE
                                recyclerView.adapter = NotificationsAdapter(notifications) { notification ->
                                    val intent = Intent(context, NotificationDetailsActivity::class.java)
                                    intent.putExtra("notification", notification)
                                    startActivity(intent)
                                }
                            }
                        } else {
                            requireActivity().runOnUiThread {
                                progressBar.visibility = View.GONE
                                Toast.makeText(context, "Failed to load notifications", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "Failed to load notifications", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
    }
}
