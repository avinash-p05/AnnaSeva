package com.techelites.annaseva

data class RequestResponse(
    val success: Boolean,
    val message: String,
    val data: RequestData
)

data class RequestData(
    val id: String,
    val donationId: String,
    val ngoId: String,
    val ngoName: String,
    val status: String,
    val priority: Double,
    val createdAt: String
)
