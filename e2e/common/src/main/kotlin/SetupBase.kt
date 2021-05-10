package com.example.common

import extension.relativePathFromResources
import mu.KotlinLogging
import java.io.File

interface SetupBase : ResourceBase {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun File.isSetupDirectory(): Boolean

    fun setupRecursively(rootPath: String, setUpFunction: (String) -> Unit) {
        getFileFromResource(rootPath).walkTopDown().forEach {
            if (it.isSetupDirectory()) {
                logger.debug { "SETUP.. ${it.relativePathFromResources()}" }
                setUpFunction(it.relativePathFromResources())
            }
        }
    }
}