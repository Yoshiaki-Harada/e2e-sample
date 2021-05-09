package com.example

import com.example.resource.SnsDb
import com.thoughtworks.gauge.Step
import org.amshove.kluent.shouldBeEqualTo

class SnsDbSpec {

    @Step("ユーザー<name>が登録される")
    fun verifyCreatedUser(name: String) {
        SnsDb.countUserByName(name) shouldBeEqualTo 1
    }

    @Step("<name>のメールアドレスが<mail>で登録される")
    fun verifyCreatedMail(name: String, mail: String) {
        SnsDb.getMailByName(name) shouldBeEqualTo mail
    }

    @Step("ユーザー<id>のnameが<name>に更新されている")
    fun verifyUpdatedName(id: String, name: String) {
        SnsDb.getNameById(id) shouldBeEqualTo name
    }

    @Step("ユーザー<id>のmailが<mail>に更新されている")
    fun verifyUpdatedMail(id: String, mail: String) {
        SnsDb.getMailById(id) shouldBeEqualTo mail
    }

    @Step("<table>の更新がされたレコードが<number>つである")
    fun verifyDiffCount(table: String, number: Int) {
        val before = SnsDb.getTable()
        val after = SnsDb.connection.createDataSet().getTable(table)
        SnsDb.diffRecordValueCount(before, after) shouldBeEqualTo number
    }
}