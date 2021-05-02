package com.example

import com.thoughtworks.gauge.BeforeSuite

class SetupAndTearDown {
    @BeforeSuite
    fun setUp() {
        setUpDb()
    }

    private fun setUpDb() {
        SnsDb.setUp()
    }
}