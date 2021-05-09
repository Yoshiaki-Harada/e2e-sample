package com.harada.driver.entity

import java.time.LocalDateTime
import java.util.*

data class CommentEntity(
    val id: UUID,
    val tweetId: UUID,
    val userId: UUID,
    val text: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CommentUpdateEntity(
    val id: UUID,
    val text: String?,
    val updatedAt: LocalDateTime
)
