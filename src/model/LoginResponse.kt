package com.example.model

data class LoginResponse(

val simpleResponse: SimpleResponse,
val user: User = User()

)
