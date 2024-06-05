package com.techelites.annaseva

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppNotification(
    val _id: String,
    val recipient: String,
    val message: String,
    val type: String,
    val metadata: NotificationMetadata,
    val read: Boolean,
    val createdAt: String,
    val __v: Int
) : Parcelable

@Parcelize
data class NotificationMetadata(
    val donationId: String,
    val trackingId: String
) : Parcelable