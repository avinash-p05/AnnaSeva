package com.techelites.annaseva

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable


data class Notification(
    val _id: String,
    val recipient: String,
    val message: String,
    val type: String,
    val metadata: Metadata,
    val read: Boolean,
    val createdAt: String,
    val __v: Int
) : Serializable


data class Metadata(
    val donationId: String,
    val ngoId: String
) : Serializable
