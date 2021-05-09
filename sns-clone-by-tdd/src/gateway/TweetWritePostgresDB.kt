package com.harada.gateway

import com.harada.domain.model.message.Tweet
import com.harada.domain.model.message.TweetId
import com.harada.domain.model.message.UpdateTweet
import com.harada.driver.dao.*
import com.harada.driver.entity.CommentEntity
import com.harada.driver.entity.CommentUpdateEntity
import com.harada.driver.entity.TweetEntity
import com.harada.driver.entity.TweetUpdateEntity
import com.harada.getDateTimeNow
import com.harada.port.TweetNotFoundException
import com.harada.port.TweetWriteStore
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class TweetWritePostgresDB(
    private val tweetDao: TweetDao.Companion,
    private val commentDao: CommentDao.Companion,
    private val db: Database,
    private val tagDao: TagDao.Companion,
    private val tagTweetMapDao: TagTweetMapDao.Companion,
    private val tagCommentMapDao: TagCommentMapDao.Companion
) : TweetWriteStore {
    override fun save(tweet: Tweet): TweetId = transaction(db) {
        tweet.replyTo?.let {
            val commentId = UUID.randomUUID()
            commentDao.create(
                CommentEntity(
                    id = commentId,
                    tweetId = it.value,
                    userId = tweet.userId.value,
                    text = tweet.text.value,
                    createdAt = getDateTimeNow(),
                    updatedAt = getDateTimeNow()
                )
            )
            tweet.tags.list.forEach {
                val tagId = tagDao.findOrCreate(it.name)
                tagCommentMapDao.create(UUID.randomUUID(), tagId, commentId)
            }
            commentId
        } ?: kotlin.run {
            val tweetId = UUID.randomUUID()
            tweetDao.create(
                TweetEntity(
                    id = tweetId,
                    userId = tweet.userId.value,
                    text = tweet.text.value,
                    createdAt = getDateTimeNow(),
                    updatedAt = getDateTimeNow()
                )
            )
            tweet.tags.list.forEach {
                val tagId = tagDao.findOrCreate(it.name)
                tagTweetMapDao.create(UUID.randomUUID(), tagId, tweetId)
            }
            tweetId
        }
    }.let {
        TweetId(it)
    }

    override fun update(tweetId: TweetId, tweet: UpdateTweet) {
        transaction(db) {
            if (tweetDao.findById(tweetId.value) != null) {
                tagTweetMapDao.deleteByTweetId(tweetId.value)
                tweet.tags?.let {
                    it.list.forEach {
                        val tagId = tagDao.findOrCreate(it.name)
                        tagTweetMapDao.create(UUID.randomUUID(), tagId, tweetId.value)
                    }
                }
                tweetDao.update(
                    TweetUpdateEntity(
                        tweetId.value,
                        text = tweet.text?.value,
                        updatedAt = getDateTimeNow()
                    )
                )
                return@transaction
            }
            if (commentDao.findById(tweetId.value) != null) {
                tagCommentMapDao.deleteByTweetId(tweetId.value)
                tweet.tags?.let {
                    it.list.forEach {
                        val tagId = tagDao.findOrCreate(it.name)
                        tagCommentMapDao.create(UUID.randomUUID(), tagId, tweetId.value)
                    }
                }
                commentDao.update(
                    CommentUpdateEntity(
                        tweetId.value,
                        text = tweet.text?.value,
                        updatedAt = getDateTimeNow()
                    )
                )
                return@transaction
            }
            throw TweetNotFoundException(tweetId = tweetId.value)
        }
    }
}

