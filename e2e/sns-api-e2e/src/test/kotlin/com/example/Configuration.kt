package com.example

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.fp.onInvalid
import com.sksamuel.hoplite.fp.onValid
import mu.KotlinLogging

data class Config(val db: Database, val api: Api) {
    data class Database(val url: String, val user: String, val pass: String)
    data class Api(val url: String)
}


object Configuration {
    private val logger = KotlinLogging.logger { }
    private val config = ConfigLoader().loadConfig<Config>("/uat.properties")
        .onInvalid {
            logger.error { "Config Loading ... ${it.description()}" }
        }.onValid {
            logger.info { it }
        }
        .getUnsafe()
    val db: Config.Database = config.db
    val api = config.api
}