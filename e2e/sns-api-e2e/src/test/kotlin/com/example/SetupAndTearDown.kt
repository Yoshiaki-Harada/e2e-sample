package com.example

import com.example.common.ResourceBase
import com.example.resource.SnsDb
import com.thoughtworks.gauge.AfterScenario
import com.thoughtworks.gauge.BeforeScenario
import com.thoughtworks.gauge.BeforeSuite
import com.thoughtworks.gauge.ExecutionContext
import extension.getSetupDirectory
import mu.KotlinLogging
import java.io.File
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

    fun getSpecsDirectoryConstructions(): List<String> {
        return File("specs")
            .walkTopDown()
            .map { it.relativeTo(File("specs")).path }
            .map { it.replace(".spec", "") }
            .filter { it.isNotBlank() }
            .sorted()
            .toList()
    }

    fun getSetupDirectoryConstructions(path: String): List<String> {
        return getFileFromResource(path).walkTopDown()
            .filter { it.isDirectory }
            .map { it.relativeTo(getFileFromResource(path)).path }
            .filter { !it.contains("sns-db") }
            .filter { it.isNotBlank() }
            .sorted()
            .toList()
    }

    @BeforeSuite()
    fun setUp() {
        val specDirectory = getSpecsDirectoryConstructions()
        val setUpDir = getSetupDirectoryConstructions("default")
        val scenarios = getSetupDirectoryConstructions("scenarios")

        println(specDirectory.toList().sorted())
        println(setUpDir.plus(scenarios).toList().distinct().sorted())
        println(setUpDir.plus(scenarios).toList().distinct().containsAll(specDirectory.toList()))

        SingleExecutor.execute {
            logger.debug { "Before Suite.." }
            setUpDb()
        }
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