package com.example.model

import io.ktor.auth.*

data class User(
    val emailId : String,
    val firstName : String,
    val lastName : String,
    val mobileNo : String ="",
    val address : String ="",
    val token : String = ""
):Principal
