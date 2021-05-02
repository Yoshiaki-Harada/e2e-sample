package com.example

import org.dbunit.database.DatabaseConnection
import java.io.File
import java.sql.DriverManager

fun File.isSetUpDirectory() =
    this.isDirectory && this.name == "sns-db" && this.containsCsv() && this.contains("table-ordering.txt")

object TweetDb {
    private val con = DriverManager.getConnection(
        Configuration.db.url,
        Configuration.db.user,
        Configuration.db.pass
    )
    private val connection = DatabaseConnection(con)

    fun setUp(rootPath: String) {
        ResourceUtil.getFileFromResource(rootPath).walkTopDown().forEach {
            if (it.isSetUpDirectory()) {
                println(it.relativePathFromResources())
                DbUtil.insertCsvData(it.relativePathFromResources(), connection)
            }
        }
    }
}