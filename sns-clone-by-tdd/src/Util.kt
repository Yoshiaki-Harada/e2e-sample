package com.harada

import java.util.*

fun getUUID(uuidStr: String): UUID {
    return uuidStr.let {
        runCatching {
            UUID.fromString(it)
        }.getOrElse {
            throw InvalidFormatIdException(uuidStr)
        }
    }
}
