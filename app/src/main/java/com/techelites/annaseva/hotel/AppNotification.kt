package com.techelites.annaseva.hotel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppNotification(
    val id: String,
    val recipientId: String,
    val title: String,
    val body: String,
    val createdAt: String,
) : Parcelable
