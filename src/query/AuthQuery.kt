package com.example.query

import com.example.auth.hash
import com.example.model.LoginAuth
import com.example.model.LoginReq
import com.example.model.User
import com.example.repository.DatabaseFactory.dbQuery
import com.example.table.LoginAuthTable
import com.example.table.UserTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

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
                t[otp] = null
            }
            LoginAuthTable.insert { t->
                t[emailId] = login.emailId
                t[hashPassword] = hash(login.password)
            }
        }
    }

    suspend fun findUserByEmail(emailId:String)= dbQuery {
        with(LoginAuthTable){
            select {
                this@with.emailId.eq(emailId)
            }.map {
                convertRowToObject(it)
            }.singleOrNull()
        }
    }

    private fun convertRowToObject(row: ResultRow?):LoginAuth?{
        if(row==null) return null
        return LoginAuth(
            row[LoginAuthTable.emailId],
            row[LoginAuthTable.hashPassword]
        )
    }

    suspend fun insertOtp(email : String,otp : String){
        dbQuery {

            UserTable.update(where = {
                UserTable.emailId.eq(email)
            }) { t->
                t[UserTable.otp] = otp
            }
        }
    }

    //for otp usage
    var c : Long = 0

    suspend fun otpExists(email : String,otp : String) : Int{

        transaction {
            c = UserTable.select { UserTable.otp.eq(otp) and UserTable.emailId.eq(email) }.count()
        }
        return c.toInt()
    }

    //update password

    suspend fun updatePassword(email : String,newPass : String){
        dbQuery {
            LoginAuthTable.update({
                LoginAuthTable.emailId.eq(email)
            }) { t->
                t[LoginAuthTable.hashPassword] = hash(newPass)
            }
        }
    }

    suspend fun removeOtp(email : String){
        dbQuery {
            UserTable.update({
                UserTable.emailId.eq(email)
            }) { t->

                t[UserTable.otp] = null
            }
        }
    }



}