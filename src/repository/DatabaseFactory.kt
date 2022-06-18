package com.example.repository

import com.example.JDBC_DATABASE_URL
import com.example.JDBC_DRIVER
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jdk.nashorn.internal.scripts.JD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {


    fun init()
    {
        Database.connect(hikari())
    }

    fun hikari() : HikariDataSource{
        val config = HikariConfig()

        config.driverClassName = JDBC_DRIVER
        config.jdbcUrl = JDBC_DATABASE_URL
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()

        print("Database Connected")
        return HikariDataSource(config)
    }

    //query function

    suspend fun <T> dbQuery(block : () -> T): T = withContext(Dispatchers.IO){
        transaction { block() }
    }

}