package com.techelites.annaseva.ngo

import java.io.Serializable

data class FoodNgo(
    val id: String?,
    val type: String?,
    val name: String?,
    val description: String?,
    val category: String?,
    val quantity: Int?,
    val expiry: String?,
    val idealfor: String?,
    val availableAt: String?,
    val location: String?,
    val transportation: String?,
    val contactPerson: String?,
    val donationStatus: String?,
    val pickupInstructions: String?,
    val hotelId: String?,
    val hotelName : String?,
    val isUsable: Boolean?,
    val autoAssignStatus: String?,
    val createdAt: String?,
    val updatedAt: String?
) : Serializable
