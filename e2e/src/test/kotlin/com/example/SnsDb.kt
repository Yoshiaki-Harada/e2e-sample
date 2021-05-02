package com.example

import mu.KotlinLogging
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConnection
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory
import java.io.File
import java.sql.DriverManager

fun File.isSetUpDirectory() =
    this.isDirectory && this.name == "sns-db" && this.containsCsv() && this.contains("table-ordering.txt")

object SnsDb {
    private val con = DriverManager.getConnection(
        Configuration.db.url,
        Configuration.db.user,
        Configuration.db.pass
    )
    private val connection = DatabaseConnection(con).apply {
        this.config.setProperty(
            DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
            PostgresqlDataTypeFactory()
        )
    }
    private val logger = KotlinLogging.logger { }

    fun setUp() {
        cleanData("clean/sns-db")
        insertData("default")
    }

    fun cleanData(path: String) {
        DbUtil.deleteAllData(path, connection)
    }

    fun insertData(rootPath: String) {
        ResourceUtil.getFileFromResource(rootPath).walkTopDown().forEach {
            if (it.isSetUpDirectory()) {
                logger.debug { "SETUP.. ${it.relativePathFromResources()}" }
                DbUtil.insertCsvData(it.relativePathFromResources(), connection)
            }
        }
    }
}