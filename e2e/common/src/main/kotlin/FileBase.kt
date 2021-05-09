package com.example.common

import java.io.File

interface FileBase {
    fun getFileFromResource(path: String): File =
        this.javaClass.classLoader
            .getResource(path)?.toURI()?.let { File(it) } ?: throw NoSuchFileException(File(path))
}