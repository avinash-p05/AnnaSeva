package com.techelites.annaseva.services

import com.techelites.annaseva.auth.LoginRequest
import com.techelites.annaseva.auth.LoginRequestVolunteer
import com.techelites.annaseva.auth.LoginResponseHotel
import com.techelites.annaseva.auth.LoginResponseNgo
import com.techelites.annaseva.auth.LoginResponseUser
import com.techelites.annaseva.auth.LoginResponseVolunteer
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

