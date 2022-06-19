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
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail


fun getUniqueNumber(length: Int) = (0..9).shuffled().take(length).joinToString("")
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
                email.setAuthenticator(DefaultAuthenticator("surajsinhrathod75@gmail.com", "yoiejaafccusbokc"))
                email.isSSLOnConnect = true
                email.setFrom("surajsinhrathod75@gmail.com")
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