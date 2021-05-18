package com.example.resource

import com.example.Configuration
import client.DataStoreClient
import client.DatabaseBaseClient
import com.example.ConstructionBase.Companion.INITIAL_DIR
import com.example.ConstructionBase.Companion.SEQUENTIAL_DIR
import com.example.common.ResourceSetupBase
import com.example.resource.tables.Users
import extension.contains
import extension.containsCsv
import mu.KotlinLogging
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConnection
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.ds.PGSimpleDataSource
import printTable
import java.io.File
import java.sql.DriverManager
import java.util.*


object SnsDb : DatabaseBaseClient, DataStoreClient, ResourceSetupBase {
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
    private val logger = KotlinLogging.logger { }

    override fun File.isSetupDirectory() =
        this.isDirectory && this.name == "sns-db" && this.containsCsv() && this.contains("table-ordering.txt")


    fun printTable() {
        val table = connection.createDataSet().getTable("users")
        table.printTable()
    }

    fun setUpDefault() {
        cleanData()
        setupRecursively(INITIAL_DIR) { insertCsvData(it) }
    }

    fun setupForScenario(directory: String) {
        cleanData()
        setupRecursively("$SEQUENTIAL_DIR/$directory") { insertCsvData(it) }
    }

    private fun cleanData() {
        logger.info { "CLEAR.. sns-db" }
        deleteAllData("clean/sns-db")
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