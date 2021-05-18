package client

import com.example.common.ResourceBase
import com.example.common.ITableBase
import extension.contains
import extension.containsCsv
import extension.relativePathFromResources
import mu.KotlinLogging
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.csv.CsvDataSet
import org.dbunit.operation.DatabaseOperation
import org.jetbrains.exposed.sql.Database
import java.io.File

interface DatabaseBaseClient : ResourceBase, ITableBase {
    val connection: IDatabaseConnection
    val db: Database

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun deleteAllData(path: String) {
        val file = getFileFromResource(path)
        check(file.contains("table-ordering.txt")) {
            "Path: $path 内にtable-ordering.txtが見つかりません"
        }
        val orders = file.listFiles()
            .first { it.name == "table-ordering.txt" }!!
            .let {
                it.readText()
            }.let {
                it.split("\n")
            }.filter { it.isNotBlank() }

        val d = orders.reversed().let {
            connection.createDataSet(it.toTypedArray())
        }
        DatabaseOperation.DELETE_ALL.execute(connection, d)
    }

    fun insertCsvData(file: File) {
        check(file.contains("table-ordering.txt")) {
            "Path: ${file.relativePathFromResources()} 内にtable-ordering.txtが見つかりません"
        }
        check(file.containsCsv()) {
            "Path: ${file.relativePathFromResources()} 内にcsvファイルが見つかりません"
        }
        val dataSet = CsvDataSet(file)
        kotlin.runCatching {
            DatabaseOperation.INSERT.execute(connection, dataSet)
        }.onFailure {
            it.cause?.let { cause ->
                logger.error(cause) { "${it.message}" }
            }
        }.getOrThrow()
    }
}