package com.techelites.annaseva.volunteer

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import com.techelites.annaseva.Notification
import com.techelites.annaseva.R

class NotificationsAdapterVolunteer(
    private val notifications: List<Notification>,
    private val onNotificationClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationsAdapterVolunteer.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_item_volunteer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = notifications.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val viewMoreButton: Button = itemView.findViewById(R.id.viewMoreButton)

        fun bind(notification: Notification) {
            messageTextView.text = notification.message
            viewMoreButton.setOnClickListener {
                handleNotificationClick(itemView.context, notification)
            }
        }

        private fun handleNotificationClick(context: Context, notification: Notification) {
            when (notification.type) {
                "QrCodeCreated" -> {
                    val qrCodePath = notification.metadata.qrCodePath
                    if (qrCodePath != null) {
                        Log.d("NotificationClick", "QrCodeCreated with path: $qrCodePath")
                        showQrCodeDialog(context, qrCodePath)
                    } else {
                        Log.e("NotificationClick", "QrCodeCreated type but qrCodePath is null")
                    }
                }
                "DonationClaimed" -> {
                    val donationId = notification.metadata.donationId
                    if (donationId != null) {
                        Log.d("NotificationClick", "DonationClaimed with donationId: $donationId")
                        loadFoodDetailsActivity(context, donationId)
                    } else {
                        Log.e("NotificationClick", "DonationClaimed type but donationId is null")
                    }
                }
                else -> {
                    val donationId = notification.metadata.donationId
                    loadFoodDetailsActivity(context, donationId)
                }
            }
        }

        private fun showQrCodeDialog(context: Context, qrCodePath: String) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_qr_code, null)
            val qrImageView: ImageView = dialogView.findViewById(R.id.qrImageView)
            val imageNumber = qrCodePath.substringAfterLast("/").substringBefore(".png")

            val imageUrl = "http://annaseva.ajinkyatechnologies.in/qrcodes/${imageNumber}.png"
            Picasso.get().load(imageUrl).into(qrImageView)

            AlertDialog.Builder(context)
                .setTitle("QR Code")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show()
        }

        private fun loadFoodDetailsActivity(context: Context, donationId: String) {
            val intent = Intent(context, VolunNotificationDetails::class.java)
            intent.putExtra("donationId", donationId)
            context.startActivity(intent)
        }
    }
}
