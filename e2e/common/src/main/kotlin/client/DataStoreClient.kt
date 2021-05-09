package client

import com.thoughtworks.gauge.datastore.ScenarioDataStore
import mu.KotlinLogging
import org.dbunit.dataset.ITable

interface DataStoreClient {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    fun storeJson(value: String) = ScenarioDataStore.put("json", value)

    fun getJson(): String = kotlin.runCatching {
        ScenarioDataStore.get("json") as String
    }.onFailure {
        logger.error {
            "jsonをDataStoreに保存しているか確認してください"
        }
    }.getOrThrow()

    fun storeTable(table: ITable) = ScenarioDataStore.put("table", table)

    fun storeTable(tableName: String, table: ITable) = ScenarioDataStore.put("table-$tableName", table)

    fun getTable(): ITable = kotlin.runCatching {
        ScenarioDataStore.get("table") as ITable
    }.onFailure {
        logger.error { "tableをDataStoreに保存しているか確認してください" }
    }.getOrThrow()

    fun getTable(tableName: String): ITable = kotlin.runCatching {
        ScenarioDataStore.get("table-$tableName") as ITable
    }.onFailure {
        logger.error { "table $tableName をDataStoreに保存しているか確認してください" }
    }.getOrThrow()
}