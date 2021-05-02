package com.example

import java.io.File

object ResourceUtil {
    fun getFileFromResource(path: String): File =
        this.javaClass.classLoader
            .getResource(path)?.toURI()?.let { File(it) } ?: throw NoSuchFileException(File(path))
}

fun File.contains(fileName: String) = this.listFiles()?.map {
    it.name
}?.contains(fileName) ?: false

fun File.containsCsv() = this.listFiles()?.filter { it.name.contains(".csv") }?.toList()?.isNotEmpty() ?: false

// uat.propertiesがいないと使えない
fun File.relativePathFromResources(): String =
    this.relativeTo(ResourceUtil.getFileFromResource("uat.properties").parentFile).path