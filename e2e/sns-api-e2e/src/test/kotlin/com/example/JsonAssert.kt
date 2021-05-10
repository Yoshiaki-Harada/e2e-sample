package com.example

import client.DataStoreClient
import com.example.common.JsonBase
import com.thoughtworks.gauge.Step
import extension.getValuesByFilterInArray
import mu.KotlinLogging
import org.amshove.kluent.*

class JsonAssert : DataStoreClient, JsonBase {
    private val logger = KotlinLogging.logger { }

    @Step("JSONのキー<key>のバリューが<value>である")
    fun verify(key: String, value: String) {
        val result = getJsonDocumentContext(getJson()).read<String>("$.$key")
        result `should be equal to` value
    }

    @Step("JSONのキー<arrayKey>のある要素のキー<key>にバリュー<value>が含まれる")
    fun verifyArray(arrayKey: String, key: String, value: String) {
        val result = getJsonDocumentContext(getJson()).read<List<String>>("$.$arrayKey.*.$key")
        result `should contain` value
    }

    @Step("JSONのキー<arrayKey>の配列において、<filterKey>が<filterValue>の要素の<key>のバリューが<value>である")
    fun verifySpecificValue(arrayKey: String, filterKey: String, filterValue: String, key: String, value: String) {
        val result = getJsonDocumentContext(getJson())
            .getValuesByFilterInArray<String>(
                arrayKey,
                filterKey,
                filterValue,
                key
            )
        result `should be equal to` listOf(value)
    }
}