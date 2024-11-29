package com.techelites.annaseva.auth

import java.io.Serializable

data class LoginRequest(val email: String, val password: String)
data class LoginRequestVolunteer(val email: String, val password: String)


data class LoginResponseHotel(
    val success: Boolean,
    val message: String,
    val data: Hotel?
)


data class LoginResponseNgo(
    val success: Boolean,
    val message: String,
    val data: Ngo?
)


data class Hotel(
    val id: String,
    val token:String,
    val name: String,
    val email: String,
    val password: String,
    val address: String,
    val city: String,
    val state: String,
    val pinCode: String,
    val location: Location,
    val contactPerson: String,
    val contactNumber: String,
    val donations: List<Any>,  // Adjust the type based on actual data
    val createdAt: String,
    val updatedAt: String,
    val verified: Boolean
)

data class Ngo(
    val id: String,
    val token:String,
    val name: String,
    val email: String,
    val password: String,
    val address: String,
    val city: String,
    val state: String,
    val pinCode: String,
    val location: Location,
    val contactPerson: String,
    val contactNumber: String,
    val requestedDonations: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val verified: Boolean
)

data class LoginResponseVolunteer(
    val success: Boolean,
    val message: String,
    val volunteer: Volunteer?
)

data class Volunteer(
    val location: Location,
    val _id: String,
    val name: String,
    val email: String,
    val password: String,
    val address: String,
    val city: String,
    val state: String,
    val pincode: String,
    val contactNumber: String,
    val requestedDonations: List<String>,
    val distributionPhotos: List<String>,
    val isVerified: Boolean,
    val teamSize: Int,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)


data class Location(
    val type: String,
    val coordinates: DoubleArray = DoubleArray(2)
)

data class DonationsResponse(
    val success: Boolean,
    val donations: List<Donation>
)

data class Donation(
    val id: String,
    val type: String,
    val name: String,
    val description: String,
    val category: String,
    val quantity: Int,
    val expiry: String,
    val idealFor: String,
    val availableAt: String,
    val location: Location,
    val transportation: String,
    val imageUrl: String,
    val requests: Map<String,String>, // Change type as per actual data
    val contactPerson: String,
    val donationStatus: String,
    val pickupInstructions: String,
    val hotel: Hotel, // Change type as per actual data
    val autoAssignStatus: String,
    val shipmentStatus: String,
    val hotelCoversTransport: Boolean,
    val createdAt: String,
    val updatedAt: String,
) : Serializable

data class UploadResponse(
    val success: Boolean,
    val message: String,
    val data: Donation
)

data class DonationRequest(
    val type: String,
    val name: String,
    val description: String,
    val category: String,
    val quantity: Int,
    val expiry: String,
    val idealFor: String,
    val availableAt: String,
    val location: Location,
    val transportation: String,
    val imageUrl: String,
    val contactPerson: String,
    val pickupInstructions: String
)