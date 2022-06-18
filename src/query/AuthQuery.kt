package com.example.query

import com.example.auth.hash
import com.example.model.LoginAuth
import com.example.model.LoginReq
import com.example.model.User
import com.example.repository.DatabaseFactory.dbQuery
import com.example.table.LoginAuthTable
import com.example.table.UserTable
import org.jetbrains.exposed.sql.insert

class AuthQuery {

    suspend fun insertUser(user: User,login: LoginReq){
        dbQuery{
            UserTable.insert { t->
                t[emailId] = user.emailId
                t[firstName]=user.firstName
                t[lastName]=user.lastName
                t[mobileNo]= user.mobileNo
                t[address] = user.address
                t[token] = user.token
            }
            LoginAuthTable.insert { t->
                t[emailId] = login.emailId
                t[hashPassword] = hash(login.password)
            }
        }
    }

}