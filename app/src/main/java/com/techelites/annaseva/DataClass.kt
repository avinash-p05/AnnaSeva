package com.techelites.annaseva

import java.io.Serializable

data class LoginRequest(val email: String, val password: String, val role: String)
data class LoginRequestVolunteer(val email: String, val password: String)

data class LoginResponseUser(
    val success: Boolean,
    val token: String,
    val user: User?
)


data class LoginResponseHotel(
    val success: Boolean,
    val token: String,
    val user: Hotel?
)



data class LoginResponseNgo(
    val success: Boolean,
    val message: String,
    val user: Ngo?
)

data class User(
    val _id: String,
    val fullName: String,
    val email: String,
    val mobileNo: String,
    val password: String,
    val city: String,
    val pincode: String,
    val isDeleted: Boolean,
    val donations: List<Any>,  // Adjust the type based on actual data
    val events: List<Any>,     // Adjust the type based on actual data
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
    val userType: String
)

data class Hotel(
    val location: Location,
    val _id: String,
    val name: String,
    val email: String,
    val password: String,
    val address: String,
    val city: String,
    val state: String,
    val pincode: String,
    val contactPerson: String,
    val contactNumber: String,
    val donations: List<Any>,  // Adjust the type based on actual data
    val isDeleted: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

data class Ngo(
    val _id: String,
    val name: String,
    val email: String,
    val password: String,
    val address: String,
    val city: String,
    val state: String,
    val pincode: String,
    val contactPerson: String,
    val contactNumber: String,
    val requestedDonations: List<String>,
    val isDeleted: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
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
    val coordinates: List<Double>
)


data class DonationsResponse(
    val success: Boolean,
    val donations: List<Donation>
)

data class Donation(
    val _id: String,
    val type: String,
    val name: String,
    val description: String,
    val category: String,
    val quantity: Int,
    val expiry: String,
    val idealfor: String,
    val availableAt: String,
    val transportation: String,
    val uploadPhoto: Any?, // Change type as per actual data
    val requests: List<Any>, // Change type as per actual data
    val contactPerson: String,
    val donationStatus: String,
    val pickupInstructions: String,
    val hotel: Hotel?, // Change type as per actual data
    val isUsable: Boolean,
    val reports: List<Any>, // Change type as per actual data
    val autoAssignStatus: String,
    val shipmentStatus: String,
    val hotelCoversTransport: Boolean,
    val platformManagesTransport: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
):Serializable