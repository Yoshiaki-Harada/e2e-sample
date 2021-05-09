package com.harada.driver.dao

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.util.*

object TagCommentMap : UUIDTable(name = "tag_comment_map") {
    val tagId = reference("tag_id", Tags)
    val commentId = reference("comment_id", Tweets)
}

class TagCommentMapDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TagCommentMapDao>(TagCommentMap) {
        fun find() = all().toList()

        fun findByMessageId(tweetId: UUID) = transaction(
            transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
            repetitionAttempts = 2
        ) {
            find {
                TagCommentMap.commentId eq tweetId
            }.toList()
        }

        fun create(id: UUID, tagId: UUID, tweetId: UUID) {
            TagCommentMapDao.new(id) {
                this.tagId = EntityID(tagId, Tags)
                this.tweetId = EntityID(tweetId, Tweets)
            }
        }

        fun deleteByTweetId(tweetId: UUID) = findByMessageId(tweetId).forEach { it.delete() }
    }

    var tagId by TagCommentMap.tagId
    var tweetId by TagCommentMap.commentId
}
