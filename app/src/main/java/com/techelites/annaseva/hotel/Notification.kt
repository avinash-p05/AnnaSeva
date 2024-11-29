package com.techelites.annaseva.hotel

import android.content.Context
import android.content.Intent
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

     fun loadNotifications() {
        progressBar.visibility = View.VISIBLE
        val sharedPreferences =requireActivity().getSharedPreferences("login",
            Context.MODE_PRIVATE
        )
        val userId = sharedPreferences.getString("userId", null) ?: return

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://anna-seva-backend.onrender.com/notification/$userId")
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
                            val notificationsJsonArray = jsonObject.getAsJsonArray("data")
                            val notifications = notificationsJsonArray.map { it.asJsonObject }.map {
                                AppNotification(
                                    id = it.get("id")?.asString ?: "",
                                    recipientId = it.get("recipientId")?.asString ?: "",
                                    title = it.get("title")?.asString ?: "",
                                    body = it.get("body")?.asString ?: "",
                                    createdAt = it.get("createdAt")?.asString ?: ""
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
                                Toast.makeText(context, "Failed to load notifications 1", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "Failed to load notifications 2", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
    }
}
