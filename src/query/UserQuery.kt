package com.example.query

import com.example.auth.hash
import com.example.model.LoginReq
import com.example.model.User
import com.example.repository.DatabaseFactory
import com.example.table.LoginAuthTable
import com.example.table.UserTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update

class UserQuery {
    suspend fun updateUserProfileDetails(user: User){
        DatabaseFactory.dbQuery {
            UserTable.update(where = {
                UserTable.emailId.eq(user.emailId)
            }){ t ->
                t[firstName] = user.firstName
                t[lastName] = user.lastName
                t[mobileNo] = user.mobileNo
                t[address] = user.address
            }
        }
    }
}