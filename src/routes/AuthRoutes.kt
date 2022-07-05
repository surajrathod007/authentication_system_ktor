package com.example.routes

import com.example.auth.JWTService
import com.example.model.LoginReq
import com.example.model.RegisterReq
import com.example.model.SimpleResponse
import com.example.model.User
import com.example.query.AuthQuery
import com.example.query.UserQuery
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail


fun getUniqueNumber(length: Int) = (0..9).shuffled().take(length).joinToString("")
fun Route.AuthRoutes(

    db : AuthQuery,
    jwtService: JWTService,
    hash : (String) -> String

){
    val uq = UserQuery()
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

    post("auth/login"){
        val loginCredential = try {
            call.receive<LoginReq>()
        }catch (e : Exception){
            call.respond(HttpStatusCode.BadRequest,SimpleResponse(false,"Inproper User Credentials"))
            return@post
        }
        try{
            val user = db.findUserByEmail(loginCredential.emailId)
            if(user == null){
                call.respond(HttpStatusCode.BadRequest,SimpleResponse(false,"User not found"))
            }else{
                if(user.hashPassword == hash(loginCredential.password)){
                    val jwtToken = jwtService.generateToken(user)
                    uq.setToken(user.emailId,jwtToken)
                    val customer = uq.findUserByEmailId(user.emailId)
                    if(customer!= null){
                        call.respond(HttpStatusCode.OK,customer!!)
                    }else{
                        call.respond(HttpStatusCode.OK,"Customer Not Found")
                    }
                }else{
                    call.respond(HttpStatusCode.BadRequest,SimpleResponse(false,"incorrect password"))
                }
            }
        }catch (e:Exception){
            call.respond(HttpStatusCode.Conflict,SimpleResponse(false,e.message?:"Something went wrong"))
        }
    }

    // Log out
    post("auth/logout"){
        val providedEmailId = try {
            call.request.queryParameters["email"]
        }catch (e:Exception){
            call.respond(HttpStatusCode.BadRequest,SimpleResponse(false,"provide email"))
            return@post
        }
        val providedToken = try {
            call.request.queryParameters["token"]
        }catch (e:Exception){
            call.respond(HttpStatusCode.BadRequest,SimpleResponse(false,"provide token"))
            return@post
        }

        if(!providedEmailId.isNullOrEmpty() && !providedToken.isNullOrEmpty()){
            val user = uq.findUserByEmailId(providedEmailId)
            if (user != null) {
                if(user.token == providedToken){
                    uq.removeToken(user.emailId)
                    call.respond(HttpStatusCode.OK,SimpleResponse(true,"Log out Success"))
                }else{
                    call.respond(HttpStatusCode.Conflict,SimpleResponse(false,"Unauthorized"))
                }
            }else{
                call.respond(HttpStatusCode.NotFound,SimpleResponse(false,"User id not found"))
            }
        }else{
            call.respond(HttpStatusCode.BadRequest,SimpleResponse(false,"Insufficient Data Provided"))
        }

    }

    // Forgot Password

    post("auth/resetpassword"){

        val email = try{
            call.request.queryParameters["email"]
        }catch (e : Exception){
            call.respond(HttpStatusCode.BadRequest, SimpleResponse(false,"Provide Email"))
            return@post
        }

        val otp = try{
            call.request.queryParameters["otp"]
        }catch (e : Exception){
            call.respond(HttpStatusCode.BadRequest, SimpleResponse(false,"Provide Otp"))
            return@post

        }

        val newpas = try{
            call.request.queryParameters["newpas"]
        }catch (e : Exception){
            call.respond(HttpStatusCode.BadRequest, SimpleResponse(false,"Provide Newpas"))
            return@post

        }

        if(db.otpExists(email!!,otp!!) == 1){

            //update password
            db.updatePassword(email,newpas!!)

            //remove otp
            db.removeOtp(email)
            call.respond(HttpStatusCode.OK,SimpleResponse(true,"Password Changed Succesfully"))

        }else{
            call.respond(HttpStatusCode.BadRequest,SimpleResponse(false,"Otp Mismatch"))
        }

    }

    // OTP Service
    post("auth/otp"){

        val emails = try{
            call.request.queryParameters["email"]
        }catch (e : Exception){
            call.respond(HttpStatusCode.BadRequest, SimpleResponse(false,"Provide Email"))
            return@post
        }


        if(!emails.isNullOrEmpty()){
            //send otp

                val otp = getUniqueNumber(4)

            //send email

            try {
                val email = SimpleEmail()
                email.hostName = "smtp.gmail.com"
                email.setSmtpPort(465)
                email.setAuthenticator(DefaultAuthenticator("bcapractical007@gmail.com", "hbfovxszpxxolayy"))
                email.isSSLOnConnect = true
                email.setFrom("bcapractical007@gmail.com")
                email.subject = "Reset/Forgot Password"
                email.setMsg("Your Otp(One Time Password) \n\n\n $otp")
                email.addTo(emails)
                email.send()

                //insert otp in database

                db.insertOtp(emails,otp)

                    call.respond(HttpStatusCode.OK,SimpleResponse(true,"Otp Sent SuccesFully"))

            }catch (e : Exception){
                call.respond(HttpStatusCode.BadRequest,SimpleResponse(false,"Otp Not Sent"))
                return@post

            }


        }
    }
}