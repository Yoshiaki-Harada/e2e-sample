package com.harada.driver.dao

import com.harada.driver.entity.CommentEntity
import com.harada.driver.entity.CommentUpdateEntity
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime
import java.util.*

object Comments : UUIDTable() {
    val tweetId = reference("tweet_id", Tweets)
    val userId = reference("user_id", Users)
    val text = varchar("text", 500)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

class CommentDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CommentDao>(Comments) {
        fun create(entity: CommentEntity) = CommentDao.new(entity.id) {
            tweetId = EntityID(entity.tweetId, Tweets)
            userId = EntityID(entity.userId, Users)
            text = entity.text
            createdAt = entity.createdAt
            updatedAt = entity.updatedAt
        }.id.value

        fun update(entity: CommentUpdateEntity) {
            CommentDao.findById(entity.id)?.let { commentDao ->
                entity.text?.let {
                    commentDao.text = it
                }
                commentDao.updatedAt = entity.updatedAt
            }
        }

        fun findByTweetId(tweetId: UUID) = CommentDao.find { Comments.tweetId eq tweetId }.toList()
    }

    var tweetId by Comments.tweetId
    var userId by Comments.userId
    var text by Comments.text
    var createdAt by Comments.createdAt
    var updatedAt by Comments.updatedAt
}