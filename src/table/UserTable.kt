package com.example.table

import org.jetbrains.exposed.sql.Table

object UserTable : Table() {
    val emailId = varchar("emailId",64)
    val firstName = varchar("firstName",64)
    val lastName = varchar("lastName",64)
    val mobileNo = varchar("mobileNo",256)
    val address = varchar("address",256)
    val token = varchar("token",64)
    override val primaryKey: PrimaryKey = PrimaryKey(emailId)
}