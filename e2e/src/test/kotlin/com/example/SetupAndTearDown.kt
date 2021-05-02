package com.example

import com.thoughtworks.gauge.BeforeSuite
import mu.KotlinLogging

class SetupAndTearDown {
    @BeforeSuite
    fun setUp() {
        println(Configuration.db)
    }

    fun setUpDb() {

    }
}