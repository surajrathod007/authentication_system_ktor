package com.example.query

import com.example.auth.hash
import com.example.model.LoginAuth
import com.example.model.LoginReq
import com.example.model.User
import com.example.repository.DatabaseFactory
import com.example.table.LoginAuthTable
import com.example.table.UserTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
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
    suspend fun findUserByEmailId(emailId: String) = DatabaseFactory.dbQuery {
        with(UserTable){
            select  {
                this@with.emailId.eq(emailId)
            }.map {
                convertRowToObject(it)
            }.singleOrNull()
        }
    }

    suspend fun setToken(emailId : String,jwtToken : String){
        DatabaseFactory.dbQuery {
            UserTable.update(where =  {
                UserTable.emailId.eq(emailId)
            }){ t->
                t[token] = jwtToken
            }
        }
    }
    suspend fun removeToken(emailId : String){
        DatabaseFactory.dbQuery {
            UserTable.update(where =  {
                UserTable.emailId.eq(emailId)
            }){ t->
                t[token] = null
            }
        }
    }

    private fun convertRowToObject(row: ResultRow?): User?{
        if(row==null) return null
        return User(
            row[UserTable.emailId],
            row[UserTable.firstName],
            row[UserTable.lastName],
            row[UserTable.mobileNo],
            row[UserTable.address],
            row[UserTable.token],
            row[UserTable.otp]
        )
    }
}