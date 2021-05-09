package com.example

import client.DataStoreClient
import com.example.common.FileBase
import com.example.common.JsonBase
import com.example.common.assert.JsonOrderAssert
import com.example.resource.SnsApi
import com.example.resource.SnsDb
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.thoughtworks.gauge.Step
import extension.getIdValueByIndex
import extension.getArray
import org.amshove.kluent.`should be equal to`
import java.time.ZonedDateTime

class SnsApiSpec : DataStoreClient, FileBase, JsonBase, JsonOrderAssert {

    @Step("ID<id>を指定してユーザーを取得する")
    fun getUser(id: String) {
        val user = SnsApi.getUser(id)
        storeJson(user)
    }

    @Step("ユーザーの一覧を取得する")
    fun getUsers() {
        val user = SnsApi.getUsers()
        storeJson(user)
    }

    @Step("<file>でユーザー作成のリクエストをする")
    fun createUser(file: String) {
        val user =
            jacksonObjectMapper().readValue<Map<String, Any>>(getFileFromResource("scenarios/user/create/$file.json"))
        SnsApi.createUser(user)
    }

    @Step("ユーザー<id>の<item>の更新のリクエストをする")
    fun updateUser(id: String, item: String) {
        SnsDb.storeUsersTable()
        val user =
            jacksonObjectMapper()
                .readValue<Map<String, Any>>(getFileFromResource("scenarios/user/update/$item.json"))
        SnsApi.updateUser(id, user)
    }

    @Step("<arrayKey>の配列が<sortKey>の降順に並んでいる")
    fun verifyOrder(arrayKey: String, sortKey: String) {
        assertOrderDesc(getJsonDocumentContext(getJson()), arrayKey, sortKey) { it: String -> ZonedDateTime.parse(it) }
    }
}