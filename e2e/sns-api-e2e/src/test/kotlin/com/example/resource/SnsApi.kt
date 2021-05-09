package com.example.resource

import HttpClient
import com.example.Configuration
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging


object SnsApi : HttpClient {
    private val baseUrl = Configuration.api.url
    private val logger = KotlinLogging.logger { }

    fun getUser(id: String): String {
        val (code, header, body) = runBlocking {
            httpGetText(uri = "$baseUrl/users/$id", headers = mapOf("content-type" to "application/json"))
        }
        return body
    }

    fun getUsers(): String {
        val (code, header, body) = runBlocking {
            httpGetText(uri = "$baseUrl/users", headers = mapOf("content-type" to "application/json"))
        }
        return body
    }

    fun createUser(user: Map<String, Any>) {
        val (code, header, body) = runBlocking {
            httpPost(uri = "$baseUrl/users", headers = mapOf("content-type" to "application/json"), params = user)
        }
    }

    fun updateUser(id: String, user: Map<String, Any>) {
        val (code, header, body) = runBlocking {
            httpPut(uri = "$baseUrl/users/$id", headers = mapOf("content-type" to "application/json"), body = user)
        }
    }
}


