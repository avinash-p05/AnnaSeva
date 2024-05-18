package com.techelites.annaseva

data class LoginResponse(val token: String, val user: User)
data class User(val id: String, val email: String, val username: String)
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, val username: String)

