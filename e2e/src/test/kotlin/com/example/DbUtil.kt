package com.example

import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.csv.CsvDataSet
import org.dbunit.operation.DatabaseOperation

object DbUtil {
    fun deleteAllData(path: String, connection: IDatabaseConnection) {
        val file = ResourceUtil.getFileFromResource(path)
        check(file.contains("table-ordering.txt")) {
            "Path: $path 内にtable-ordering.txtが見つかりません"
        }
        check(file.containsCsv()) {
            "Path: $path csvファイルが見つかりません"
        }
        val dataSet = CsvDataSet(file)
        DatabaseOperation.DELETE_ALL.execute(connection, dataSet)
    }

    fun insertCsvData(path: String, connection: IDatabaseConnection) {
        val file = ResourceUtil.getFileFromResource(path)
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