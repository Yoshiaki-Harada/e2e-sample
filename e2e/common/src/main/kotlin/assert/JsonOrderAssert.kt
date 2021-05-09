package com.example.common.assert

import com.jayway.jsonpath.DocumentContext
import extension.getArray
import org.amshove.kluent.`should be equal to`

sealed class Order {
    object DESC : Order()
    object ASC : Order()
}

interface JsonOrderAssert {
    fun <T, U : Comparable<U>> assertOrderDesc(
        json: DocumentContext,
        arrayKey: String,
        sorKey: String,
        parser: (T) -> U,
    ) {
        val arrays = json.getArray(arrayKey)
        assertOrder(arrays, sorKey, parser, Order.DESC)
    }

    fun <T, U : Comparable<U>> assertOrderAsc(
        json: DocumentContext,
        arrayKey: String,
        sorKey: String,
        parser: (T) -> U,
    ) {
        val arrays = json.getArray(arrayKey)
        assertOrder(arrays, sorKey, parser, Order.ASC)
    }

    fun <T, U : Comparable<U>> assertOrder(
        json: DocumentContext,
        arrayKey: String,
        sorKey: String,
        parser: (T) -> U,
        order: Order
    ) {
        val arrays = json.getArray(arrayKey)
        assertOrder(arrays, sorKey, parser, order)
    }

    fun <T, U : Comparable<U>> assertOrder(
        jsonArray: List<Map<String, Any>>,
        sorKey: String,
        parser: (T) -> U,
        order: Order
    ) {
        val sorted = when (order) {
            is Order.DESC -> jsonArray
                .sortedByDescending {
                    parser(it[sorKey]!! as T)
                }
            is Order.ASC -> jsonArray
                .sortedBy {
                    parser(it[sorKey]!! as T)
                }
        }
        jsonArray `should be equal to` sorted
    }
}