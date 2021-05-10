package com.example

import com.example.common.ResourceBase
import com.example.resource.SnsDb
import com.thoughtworks.gauge.AfterScenario
import com.thoughtworks.gauge.BeforeScenario
import com.thoughtworks.gauge.BeforeSuite
import com.thoughtworks.gauge.ExecutionContext
import extension.getSetupDirectory
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicBoolean


object SingleExecutor {
    private val done: AtomicBoolean = AtomicBoolean(false)
    fun execute(block: () -> Any) {
        if (done.get()) return
        done.set(true)
        block()
    }
}

class SetupAndTearDown : ResourceBase {
    private val logger = KotlinLogging.logger { }

    @BeforeSuite()
    fun setUp() {
        SingleExecutor.execute {
            logger.debug { "Before Suite.." }
            setUpDb()
        }
    }

    @BeforeScenario(tags = ["sequential"])
    fun setUpScenario(context: ExecutionContext) {
        context.getSetupDirectory()?.let {
            logger.debug { "Before Scenario.." }
            SnsDb.setUpForScenario(it)
        }
    }

    @AfterScenario(tags = ["sequential"])
    fun teardownScenario(context: ExecutionContext) {
        logger.debug { "After Scenario.." }
        if (context.currentScenario.isFailing) SnsDb.printTable()
        setUpDb()
    }

    private fun setUpDb() {
        SnsDb.setUpDefault()
    }
}