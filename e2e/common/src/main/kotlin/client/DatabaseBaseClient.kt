package client

import com.example.common.FileBase
import com.example.common.ITableBase
import extension.contains
import extension.containsCsv
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.csv.CsvDataSet
import org.dbunit.operation.DatabaseOperation
import org.jetbrains.exposed.sql.Database

interface DatabaseBaseClient : FileBase, ITableBase {
    val connection: IDatabaseConnection
    val db: Database
    fun deleteAllData(path: String) {
        val file = getFileFromResource(path)
        check(file.contains("table-ordering.txt")) {
            "Path: $path 内にtable-ordering.txtが見つかりません"
        }
        check(file.containsCsv()) {
            "Path: $path csvファイルが見つかりません"
        }
        val dataSet = CsvDataSet(file)
        DatabaseOperation.DELETE_ALL.execute(connection, dataSet)
    }

    fun insertCsvData(path: String) {
        val file = getFileFromResource(path)
        check(file.contains("table-ordering.txt")) {
            "Path: $path 内にtable-ordering.txtが見つかりません"
        }
        check(file.containsCsv()) {
            "Path: $path 内にcsvファイルが見つかりません"
        }
        val dataSet = CsvDataSet(file)
        DatabaseOperation.INSERT.execute(connection, dataSet)
    }
}