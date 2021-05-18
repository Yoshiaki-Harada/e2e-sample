import com.example.common.ColumnRecordMap
import com.example.common.PrimaryKeys
import mu.KotlinLogging
import org.dbunit.dataset.ITable

private val logger = KotlinLogging.logger("ITable Extensions Function")

fun ITable.createPrimaryKeyValues(index: Int): PrimaryKeys = this
    .tableMetaData
    .primaryKeys
    .map { this.getValue(index, it.columnName) }

fun ITable.createRecordMap(index: Int): ColumnRecordMap = this.tableMetaData
    .columns
    .fold(mapOf<String, Any>()) { tmp, column ->
        tmp.plus(column.columnName to this.getValue(index, column.columnName))
    }

fun ITable.createMap(): Map<PrimaryKeys, ColumnRecordMap> = List(this.rowCount) { it }
    .fold(mapOf()) { tmp, index ->
        tmp.plus(this.createPrimaryKeyValues(index) to this.createRecordMap(index))
    }

fun ITable.printTable() {
    val map = this.createMap()
    logger.info { "${this.tableMetaData.tableName}: " }
    logger.info {
        map.values.toList()
    }
}
