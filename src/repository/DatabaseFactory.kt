package com.example.repository

import com.example.JDBC
import com.example.table.LoginAuthTable
import com.example.table.UserTable

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

object DatabaseFactory {


    fun init()
    {
        Database.connect(hikari())
        transaction {
            SchemaUtils.create(UserTable)
            SchemaUtils.create(LoginAuthTable)
        }
    }

    fun hikari() : HikariDataSource{
        val config = HikariConfig()

        config.driverClassName = JDBC.JDBC_DRIVER
//        config.jdbcUrl = JDBC.JDBC_DATABASE_URL
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"

        val uri = URI(System.getenv("DATABASE_URL"))
        val userName = uri.userInfo.split(":").toTypedArray()[0]
        val password = uri.userInfo.split(":").toTypedArray()[1]

        config.jdbcUrl = "jdbc:postgresql://" + uri.host + ":" + uri.port + uri.path + "?sslmode=require" + "&user=$userName&password=$password"
        config.validate()

        print("Database Connected")
        return HikariDataSource(config)
    }

    //query function

    suspend fun <T> dbQuery(block : () -> T): T = withContext(Dispatchers.IO){
        transaction { block() }
    }

}