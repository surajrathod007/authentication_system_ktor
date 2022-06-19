package com.example.routes

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

fun Route.UserRoutes(db : UserQuery){

    post("user/update_profile"){
        val userBody = try{
            call.receive<User>()
        }catch (e:Exception){
            call.respond(HttpStatusCode.BadRequest, SimpleResponse(false,"Improper User Data"))
            return@post
        }

        try {
            db.updateUserProfileDetails(userBody)
            call.respond(HttpStatusCode.OK, SimpleResponse(true,"Profile Updated"))
        }catch (e:Exception){
            call.respond(HttpStatusCode.Conflict, SimpleResponse(false,"$e Occured"))
        }
    }

}