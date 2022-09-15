package com.example

import com.example.auth.JWTService
import com.example.auth.hash
import com.example.query.AuthQuery
import com.example.query.UserQuery
import com.example.repository.DatabaseFactory
import com.example.routes.AuthRoutes
import com.example.routes.UserRoutes
import com.example.table.UserTable
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.sessions.*
import io.ktor.auth.*
import io.ktor.gson.*
import io.ktor.features.*
import org.jetbrains.exposed.sql.selectAll

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

   
    val authDb = AuthQuery()
    val userDb = UserQuery()
    val jwtService = JWTService()
    val makeHash = {s:String -> hash(s) }

    DatabaseFactory.init()


    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(Authentication) {
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {

        AuthRoutes(authDb,jwtService,makeHash)
        UserRoutes(userDb)
        get("/stats") {
            call.respondText("HELLO MANSH!", contentType = ContentType.Text.Plain)
            // Should print complete table
            call.respondText( "${UserTable.selectAll()}")
        }
    }
}

data class MySession(val count: Int = 0)

