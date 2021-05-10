package extension

import com.jayway.jsonpath.DocumentContext
import com.thoughtworks.gauge.ExecutionContext

fun DocumentContext.getIdValueByIndex(
    arrayKey: String,
    idKey: String,
    index: Int
): String = this.read("$.$arrayKey[$index].$idKey")

fun DocumentContext.getArray(
    arrayKey: String
): List<Map<String, Any>> = this.read("$.$arrayKey")

fun <T> DocumentContext.getValuesByFilterInArray(
    arrayKey: String,
    filterKey: String,
    filterValue: String,
    key: String
): List<T> = this.read("$.$arrayKey[?(@.$filterKey == '$filterValue')].$key")
