package com.example.common

import extension.relativePathFromResources
import mu.KotlinLogging
import java.io.File

interface ResourceSetupBase : ResourceBase {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun File.isSetupDirectory(): Boolean

    fun setupRecursively(rootPath: String, setupFunction: (File) -> Unit) {
        getFileFromResource(rootPath).walkTopDown().forEach {
            if (it.isSetupDirectory()) {
                logger.info { "SETUP.. ${it.relativePathFromResources()}" }
                setupFunction(it)
            }
        }
    }
}