package com.example.resource

import com.example.Configuration
import com.example.common.DataStoreClient
import com.example.common.DatabaseBase
import com.example.common.FileBase
import com.example.resource.tables.Users
import mu.KotlinLogging
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConnection
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.ds.PGSimpleDataSource
import java.io.File
import java.sql.DriverManager
import java.util.*


object SnsDb : DatabaseBase, DataStoreClient, FileBase {
    override val db = Database.connect(
        PGSimpleDataSource().apply {
            user = Configuration.db.user
            password = Configuration.db.pass
            setURL(Configuration.db.url)
        })

    override val connection = DatabaseConnection(
        DriverManager.getConnection(
            Configuration.db.url,
            Configuration.db.user,
            Configuration.db.pass
        )
    ).apply {
        this.config.setProperty(
            DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
            PostgresqlDataTypeFactory()
        )
    }

    fun File.isSetUpDirectory() =
        this.isDirectory && this.name == "sns-db" && this.containsCsv() && this.contains("table-ordering.txt")

    private val logger = KotlinLogging.logger { }

    fun printTable() {
        val table = connection.createDataSet().getTable("users")
        table.printTable()
    }

    fun setUpDefault() {
        cleanData()
        insertData("default")
    }

    fun setUpForScenario(directory: String) {
        cleanData()
        insertData("scenarios/$directory")
    }

    private fun cleanData() {
        logger.debug { "CLEAR.. sns-db" }
        deleteAllData("clean/sns-db")
    }

    private fun insertData(rootPath: String) {
        getFileFromResource(rootPath).walkTopDown().forEach {
            if (it.isSetUpDirectory()) {
                logger.debug { "SETUP.. ${it.relativePathFromResources()}" }
                insertCsvData(it.relativePathFromResources())
            }
        }
    }

    fun storeUsersTable() {
        val table = connection.createDataSet().getTable("users")
        storeTable(table)
    }

    fun countUserByName(name: String) = transaction {
        Users.select { Users.name eq name }.count()
    }

    fun getMailByName(name: String) = transaction {
        Users.select { Users.name eq name }.single()[Users.mail]
    }

    fun getNameById(id: String) = transaction {
        Users.select { Users.id eq UUID.fromString(id) }.single()[Users.name]
    }

    fun getMailById(id: String) = transaction {
        Users.select { Users.id eq UUID.fromString(id) }.single()[Users.mail]
    }
}