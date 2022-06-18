package com.example.routes

import com.example.auth.JWTService
import com.example.model.LoginReq
import com.example.model.RegisterReq
import com.example.model.SimpleResponse
import com.example.model.User
import com.example.query.AuthQuery
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.AuthRoutes(

    db : AuthQuery,
    jwtService: JWTService,
    hash : (String) -> String

){
    // Register

    post("auth/register") {
        val userBody = try{
            call.receive<RegisterReq>()
        }catch (e:Exception){
            call.respond(HttpStatusCode.BadRequest, SimpleResponse(false,"Improper User Data"))
            return@post
        }

        try {
            val user = User(userBody.emailId,userBody.firstName,userBody.lastName)
            db.insertUser(user,LoginReq(user.emailId,userBody.password))
            call.respond(HttpStatusCode.OK,SimpleResponse(true,"Registration Done"))
        }catch (e:Exception){
            call.respond(HttpStatusCode.Conflict,SimpleResponse(false,"$e Occured"))
        }
    }

    // Login

    // Forgot Password

    // OTP Service
}