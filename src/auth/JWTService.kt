package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.model.LoginAuth
import com.example.model.User
import javax.xml.crypto.AlgorithmMethod

class JWTService {
    val issuer = "authServer"
    val secret = "authorized"
    val algorithm = Algorithm.HMAC512(secret)

    val verifier : JWTVerifier = JWT.require(algorithm).withIssuer(issuer).build()


    fun generateToken(user: LoginAuth):String{
        return JWT.create()
            .withSubject("makeAuthorized")
            .withIssuer(issuer)
            .withClaim("emailId",user.emailId)
            .sign(algorithm)
    }
}