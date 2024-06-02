package com.techelites.annaseva.services

import com.techelites.annaseva.LoginRequest
import com.techelites.annaseva.LoginRequestVolunteer
import com.techelites.annaseva.LoginResponseHotel
import com.techelites.annaseva.LoginResponseNgo
import com.techelites.annaseva.LoginResponseUser
import com.techelites.annaseva.LoginResponseVolunteer
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("user/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<LoginResponseUser>

    @POST("user/login")
    fun loginHotel(@Body loginRequest: LoginRequest): Call<LoginResponseHotel>

    @POST("user/login")
    fun loginNgo(@Body loginRequest: LoginRequest): Call<LoginResponseNgo>

    @POST("volunteer/login")
    fun loginVolunteer(@Body loginRequest: LoginRequestVolunteer): Call<LoginResponseVolunteer>
}

