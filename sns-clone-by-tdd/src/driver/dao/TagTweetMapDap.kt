package com.harada.driver.dao

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.util.*

object TagTweetMap : UUIDTable(name = "tag_tweet_map") {
    val tagId = reference("tag_id", Tags)
    val tweetId = reference("tweet_id", Tweets)
}

class TagTweetMapDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TagTweetMapDao>(TagTweetMap) {
        fun find() = all().toList()

        fun findByTweetId(tweetId: UUID) = transaction(
            transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
            repetitionAttempts = 2
        ) {
            find {
                TagTweetMap.tweetId eq tweetId
            }.toList()
        }

        fun create(id: UUID, tagId: UUID, tweetId: UUID) {
            TagTweetMapDao.new(id) {
                this.tagId = EntityID(tagId, Tags)
                this.tweetId = EntityID(tweetId, Tweets)
            }
        }

        fun deleteByTweetId(tweetId: UUID) = findByTweetId(tweetId).forEach { it.delete() }
    }

    var tagId by TagTweetMap.tagId
    var tweetId by TagTweetMap.tweetId
}
