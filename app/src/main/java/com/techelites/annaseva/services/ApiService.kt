package com.techelites.annaseva.services

import com.techelites.annaseva.auth.Donation
import com.techelites.annaseva.auth.DonationRequest
import com.techelites.annaseva.auth.LoginRequest
import com.techelites.annaseva.auth.LoginRequestVolunteer
import com.techelites.annaseva.auth.LoginResponseHotel
import com.techelites.annaseva.auth.LoginResponseNgo
import com.techelites.annaseva.auth.LoginResponseVolunteer
import com.techelites.annaseva.auth.UploadResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("auth/hotel/login")
    fun loginHotel(@Body loginRequest: LoginRequest): Call<LoginResponseHotel>

    @POST("auth/ngo/login")
    fun loginNgo(@Body loginRequest: LoginRequest): Call<LoginResponseNgo>

    @POST("volunteer/login")
    fun loginVolunteer(@Body loginRequest: LoginRequestVolunteer): Call<LoginResponseVolunteer>

    @POST("donation/createDonation/{id}")
    fun postDonation(@Path("id") email: String ,@Body uploadRequest: DonationRequest): Call<UploadResponse>

}

