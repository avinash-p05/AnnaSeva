package com.techelites.annaseva.ngo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.techelites.annaseva.Notification
import com.techelites.annaseva.R
import com.techelites.annaseva.hotel.AppNotification

class NotificationsAdapterNgo(
    private val notifications: List<AppNotification>,
    private val onNotificationClick: (AppNotification) -> Unit
) : RecyclerView.Adapter<NotificationsAdapterNgo.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_item_ngo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int = notifications.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextViewN)
        private val title: TextView = itemView.findViewById(R.id.titleTextViewN)

        fun bind(notification: AppNotification) {
            messageTextView.text = notification.body
            title.text = notification.title
        }
    }
}
