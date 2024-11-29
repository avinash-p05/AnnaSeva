package com.techelites.annaseva

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.techelites.annaseva.ngo.NgoFoodListings
import com.techelites.annaseva.ngo.NgoMainActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException

class MyFirebaseMessagingService : FirebaseMessagingService() {

    fun onNewToken(token: String,userId: String) {
        super.onNewToken(token)
        Log.d("MyFirebaseMessagingService", "Refreshed token: $token")

        Log.d("UserId", "id: $userId")
        // Check if userId is not null before sending the token to the server
            sendTokenToServer(token, userId)
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("MyFirebaseMessagingService", "From: ${remoteMessage.from}")

        // Check if the message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d("MyFirebaseMessagingService", "Message data payload: ${remoteMessage.data}")
            handleDataPayload(remoteMessage.data)
        }

        // Check if the message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("MyFirebaseMessagingService", "Message Notification Body: ${it.body}")
            handleNotification(it)
        }
    }

    fun sendTokenToServer(token: String,userId:String) {
        val client = OkHttpClient()

        // Include the token as a query parameter in the URL
        val url = "https://anna-seva-backend.onrender.com/auth/token/$userId?token=$token"

        // Since the token is now in the URL, no need for a JSON body
        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create(null, ByteArray(0))) // Use an empty body for the POST request
            .build()

        Log.e("Login", "Token - $token")
        Log.e("Login", "request - $request")

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("Login", "Failed to send token to server: ${e.message}", e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    Log.e("Login", "Failed to send token to server: ${response.message}, Response Code: ${response.code}")
                } else {
                    Log.d("Login", "Token sent successfully")
                }
            }
        })
    }

    private fun handleDataPayload(data: Map<String, String>) {
        // Handle the data payload received in the message
        // For example, you might want to start an activity, update the UI, or handle the data in the background
        val title = data["title"]
        val message = data["message"]

        // Show a notification
        if (!title.isNullOrEmpty() && !message.isNullOrEmpty()) {
            showNotification(title, message)
        }
    }

    private fun handleNotification(notification: RemoteMessage.Notification) {
        // Handle the notification payload
        notification.body?.let { body ->
            showNotification(notification.title ?: "New Notification", body)
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "default_channel"

        val intent = Intent(this, NgoMainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0, // Request code (can be used to distinguish multiple intents)
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Ensure it's immutable for API 31+
        )


        // Create the notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Default Channel for App Notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo) // Set your app's notification icon here
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Show the notification
        notificationManager.notify(0, notificationBuilder.build())
    }
}
