package com.example

import com.example.common.ResourceBase
import mu.KotlinLogging
import java.io.File
import java.lang.IllegalStateException

interface ConstructionBase : ResourceBase {
    companion object {
        const val SEQUENTIAL_DIR = "sequential"
        const val INITIAL_DIR = "initial"
        val resources = listOf("sns-db")
        private val logger = KotlinLogging.logger {}
    }

    /**
     * specsディレクトリの構成とresources以下のセットアップのディレクトリ構成が揃っていないと例外をスローする
     * specs/aaa/bbb.spec
     *          /ccc.spec
     * resources/...
     *      /aaa/bbb
     *          /ccc
     */
    fun checkDirectoryConstructions() {
        val expectedConstructions = getExpectedSetupDirectoryConstructions()
        val setupConstructions = getSetupDirectoryConstructions()

        if (expectedConstructions != setupConstructions.sorted()) {
            logger.error { "specs constructions $expectedConstructions but setup Constructions $setupConstructions" }
            throw IllegalStateException("specsとsetupのディレクトリ構成が違います")
        }
    }

    private fun getSetupDirectoryConstructions(): List<String> {
        return getSetupDirectoryConstructions(INITIAL_DIR)
            .plus(getSetupDirectoryConstructions(SEQUENTIAL_DIR))
            .distinct()
    }

    private fun getExpectedSetupDirectoryConstructions(): List<String> {
        return File("specs")
            .walkTopDown()
            .map { it.relativeTo(File("specs")).path }
            .map { it.replace(".spec", "") }
            .filter { it.isNotBlank() }
            .sorted()
            .toList()
    }

    private fun getSetupDirectoryConstructions(rootPath: String): List<String> {
        return getFileFromResource(rootPath)
            .walkTopDown()
            .filter { it.isDirectory }
            .map { it.relativeTo(getFileFromResource(rootPath)).path }
            .filterNot { it.contains(Regex(resources.joinToString("|"))) }
            .filter { it.isNotBlank() }
            .sorted()
            .toList()
    }
}