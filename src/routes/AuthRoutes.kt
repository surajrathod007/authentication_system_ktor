package com.example.routes

import com.example.auth.JWTService
import com.example.model.*
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
            call.respond(HttpStatusCode.OK,LoginResponse(SimpleResponse(false,"Inproper User Credentials"),))
            return@post
        }

        val user = db.findUserByEmail(loginCredential.emailId)

        if(user == null) {
            call.respond(HttpStatusCode.OK, LoginResponse(SimpleResponse(false, "User not found")))
            return@post
        }

        if(user.hashPassword != hash(loginCredential.password)) {
            call.respond(HttpStatusCode.OK, LoginResponse(SimpleResponse(false, "Wrong Password")))
            return@post
        }

        val jwtToken = jwtService.generateToken(user)
        uq.setToken(user.emailId,jwtToken)

        val customer = uq.findUserByEmailId(user.emailId)
        if(customer== null){
            call.respond(HttpStatusCode.OK,LoginResponse(SimpleResponse(false,"Account Not Found")))
            return@post
        }

        call.respond(HttpStatusCode.OK,LoginResponse(SimpleResponse(true,"Login Success"),customer))

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

        if(providedEmailId.isNullOrEmpty() || providedToken.isNullOrEmpty()){
            call.respond(HttpStatusCode.BadRequest,SimpleResponse(false,"Insufficient Data Provided"))
            return@post
        }

        val userAuth = db.findUserByEmail(providedEmailId)
        if(userAuth==null){
            call.respond(HttpStatusCode.Conflict,SimpleResponse(false,"User id not found"))
            return@post
        }

        val user = uq.findUserByEmailId(userAuth.emailId)
        if (user == null) {
            call.respond(HttpStatusCode.Conflict,SimpleResponse(false,"cant find user"))
            return@post
        }

        if(user.token != providedToken) {
            call.respond(HttpStatusCode.Conflict, SimpleResponse(false, "Unauthorized"))
            return@post
        }

        uq.removeToken(user.emailId)
        call.respond(HttpStatusCode.OK,SimpleResponse(true,"Log out Success"))
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

        if(db.isEmailExists(emails!!)==0){
            call.respond(HttpStatusCode.NotFound,SimpleResponse(false,"Email Does Not Exists In Our Database !"))
            return@post
        }


        if(emails.isNullOrEmpty()) {
            call.respond(HttpStatusCode.OK,SimpleResponse(false,"Invalid Email"))
            return@post
        }
        val user = db.findUserByEmail(emails)
        if(user==null){
            call.respond(HttpStatusCode.OK,SimpleResponse(false,"Account Doesn't Exist"))
            return@post
        }

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
                call.respond(HttpStatusCode.OK,SimpleResponse(false,"Otp Not Sent"))
                return@post

            }

    }
}