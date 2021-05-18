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
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");
        SingleExecutor.execute {
            logger.info { "Before Suite.." }
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
            logger.info { "Before Scenario.." }
            SnsDb.setupForScenario(it)
        }
    }

    @AfterScenario(tags = ["sequential"])
    fun teardownScenario(context: ExecutionContext) {
        logger.info { "After Scenario.." }
        if (context.currentScenario.isFailing) SnsDb.printTable()
        setUpDb()
    }

    private fun setUpDb() {
        SnsDb.setUpDefault()
    }
}