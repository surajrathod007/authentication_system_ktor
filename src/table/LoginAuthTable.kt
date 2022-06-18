package com.example.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object LoginAuthTable : Table() {
    val emailId = varchar("emailId",64)
    val hashPassword = varchar("hashPassword",512)
    override val primaryKey: PrimaryKey = PrimaryKey(emailId)
}