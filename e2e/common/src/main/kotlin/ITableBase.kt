package com.example.common

import createMap
import mu.KotlinLogging
import org.dbunit.dataset.ITable

typealias PrimaryKeys = List<Any>
typealias ColumnRecordMap = Map<String, Any>

interface ITableBase {
    companion object {
        val logger = KotlinLogging.logger { }
    }

    fun diffValueCount(m1: Map<PrimaryKeys, ColumnRecordMap>, m2: Map<PrimaryKeys, ColumnRecordMap>): Int {
        check(m1.keys.size == m2.keys.size) {
            "要素数が異なります"
        }
        var count = 0
        m1.entries.forEach {
            if (m2[it.key] != it.value) count += 1
        }
        return count
    }

    fun diffRecordValueCount(table1: ITable, table2: ITable): Int {
        check(table1.tableMetaData == table2.tableMetaData) {
            "Tableが異なります"
        }
        val map1 = table1.createMap()
        val map2 = table2.createMap()
        return diffValueCount(map1, map2)
    }
}