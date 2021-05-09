package com.harada.driver.dao

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.select
import java.util.*

object Tags : UUIDTable() {
    val name = varchar("name", 100)
}

class TagDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TagDao>(Tags) {

        fun find() = all().toList()

        fun findByName(name: String) =
            TagDao.find { Tags.name eq name }.firstOrNull()

        fun create(id: UUID, tagName: String) = TagDao.new(id) {
            name = tagName
        }

        fun findOrCreate(name: String): UUID {
            return findByName(name)?.id?.value ?: let {
                val tagId = UUID.randomUUID()
                create(tagId, name).id.value
            }
        }

        fun findByTweetId(tweetId: UUID) =
            Tags.join(TagTweetMap, JoinType.LEFT, TagTweetMap.tagId).slice(Tags.columns).select {
                TagTweetMap.tweetId eq tweetId
            }

        fun findTagNamesByTweetId(tweetId: UUID) = findByTweetId(tweetId).map { resultRow -> resultRow[Tags.name] }
        fun findByCommentId(commentId: UUID) =
            Tags.join(TagCommentMap, JoinType.LEFT, TagCommentMap.tagId).slice(Tags.columns).select {
                TagCommentMap.commentId eq commentId
            }

        fun findTagNamesByCommentId(commentId: UUID) =
            findByCommentId(commentId).map { resultRow -> resultRow[Tags.name] }
    }

    var name by Tags.name
}
