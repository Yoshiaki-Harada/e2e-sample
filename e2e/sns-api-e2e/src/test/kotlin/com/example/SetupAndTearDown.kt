package com.example


import com.example.resource.SnsDb
import com.thoughtworks.gauge.AfterScenario
import com.thoughtworks.gauge.BeforeScenario
import com.thoughtworks.gauge.BeforeSuite
import com.thoughtworks.gauge.ExecutionContext
import extension.getSetupDirectory
import mu.KotlinLogging
import kotlin.IllegalStateException


class SetupAndTearDown : ConstructionBase {
    private val logger = KotlinLogging.logger { }


    @BeforeSuite()
    fun setUp() {
        checkDirectoryConstructions()
        SingleExecutor.execute {
            logger.debug { "Before Suite.." }
            setUpDb()
        }
    }

    @BeforeScenario()
    fun checkSequential(context: ExecutionContext) {
        if (context.getSetupDirectory() == null) return
        if (!context.allTags.contains("sequential"))
            throw IllegalStateException("tag:sequentialがついていない場合 ${context.getSetupDirectory()} はセットアップされません。")
    }

    @BeforeScenario(tags = ["sequential"])
    fun setUpScenario(context: ExecutionContext) {
        context.getSetupDirectory()?.let {
            logger.debug { "Before Scenario.." }
            SnsDb.setupForScenario(it)
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