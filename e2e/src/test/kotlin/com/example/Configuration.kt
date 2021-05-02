package com.example

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.fp.onInvalid
import mu.KotlinLogging

data class Config(val db: Database, val api: Api) {
    data class Database(val url: String, val user: String, val pass: String)
    data class Api(val url: String)
}

val logger = KotlinLogging.logger { }

object Configuration {
    private val config = ConfigLoader().loadConfig<Config>("/uat.properties")
        .onInvalid {
            logger.error { "Config Loading ... ${it.description()}" }
        }.getUnsafe()
    val db: Config.Database = config.db
    val api = config.api
}