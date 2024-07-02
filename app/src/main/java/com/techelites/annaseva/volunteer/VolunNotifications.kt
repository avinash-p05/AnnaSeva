package com.techelites.annaseva.volunteer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser
import com.techelites.annaseva.Metadata
import com.techelites.annaseva.Notification
import com.techelites.annaseva.R
import com.techelites.annaseva.ngo.NgoNotificationsDetails
import com.techelites.annaseva.ngo.NotificationsAdapterNgo
import okhttp3.*
import java.io.IOException

class VolunNotifications : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_volun_notifications, container, false)
        recyclerView = view.findViewById(R.id.recipeViewT)
        progressBar = view.findViewById(R.id.progressBarT)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadNotifications()
        return view
    }

    private fun loadNotifications() {
        progressBar.visibility = View.VISIBLE
        val sharedPreferences = requireActivity().getSharedPreferences("login", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userid", null) ?: return

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://annaseva.ajinkyatechnologies.in/api/donation/notifications/$userId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        try {
                            val responseString = responseBody.string()
                            Log.d("NgoNotifications", "Response JSON: $responseString")

                            val jsonObject = JsonParser.parseString(responseString).asJsonObject
                            val success = jsonObject.get("success")?.asBoolean ?: false

                            if (success) {
                                val notificationsJsonArray = jsonObject.getAsJsonArray("notifications")
                                val notifications = notificationsJsonArray.mapNotNull { it.asJsonObject }.map {
                                    val metadataObject = it.getAsJsonObject("metadata")
                                    val donationId = metadataObject?.get("donationId")?.asString ?: ""
                                    val qrCodePath = metadataObject?.get("qrCodePath")?.asString ?: ""

                                    Notification(
                                        _id = it.get("_id")?.asString ?: "",
                                        recipient = it.get("recipient")?.asString ?: "",
                                        message = it.get("message")?.asString ?: "",
                                        type = it.get("type")?.asString ?: "",
                                        metadata = Metadata(donationId = donationId, qrCodePath = qrCodePath),
                                        read = it.get("read")?.asBoolean ?: false,
                                        createdAt = it.get("createdAt")?.asString ?: "",
                                    )
                                }

                                requireActivity().runOnUiThread {
                                    progressBar.visibility = View.GONE
                                    recyclerView.adapter = NotificationsAdapterVolunteer(notifications) { notification ->
                                        val intent = Intent(requireContext(), NgoNotificationsDetails::class.java)
                                        intent.putExtra("notification", notification)
                                        startActivity(intent)
                                    }
                                }
                            } else {
                                requireActivity().runOnUiThread {
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(requireContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            requireActivity().runOnUiThread {
                                progressBar.visibility = View.GONE
                                Log.e("NgoNotifications", "Error: ${e.message}")
                                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
