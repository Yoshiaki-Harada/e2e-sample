package com.harada.driver.entity

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class UserEntity(
    val id:UUID,
    val name: String,
    val mail: String,
    val birthday: LocalDate,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class UserUpdateEntity(
    val id:UUID,
    val name: String?,
    val mail: String?,
    val birthday: LocalDate?,
    val updatedAt: LocalDateTime
)
