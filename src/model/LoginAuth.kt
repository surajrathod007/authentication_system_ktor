package com.example.model

import io.ktor.auth.*

data class LoginAuth(
    val emailId : String,
    val hashPassword : String
):Principal
