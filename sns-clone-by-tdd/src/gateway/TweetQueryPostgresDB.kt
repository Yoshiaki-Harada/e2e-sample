package com.harada.gateway

import com.harada.domain.model.message.TweetId
import com.harada.domainmodel.tweet.TweetFilter
import com.harada.driver.dao.*
import com.harada.formatter
import com.harada.port.TweetNotFoundException
import com.harada.port.TweetQueryService
import com.harada.port.UserNotFoundException
import com.harada.viewmodel.ReplyInfo
import com.harada.viewmodel.TimeLine
import com.harada.viewmodel.TweetInfo
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.time.ZoneId
import java.util.*

class TweetQueryPostgresDB(
    private val tweetDao: TweetDao.Companion,
    private val userDao: UserDao.Companion,
    private val commentDao: CommentDao.Companion,
    private val db: Database,
    private val tagDao: TagDao.Companion
) : TweetQueryService {
    val logger = KotlinLogging.logger { }
    override fun getTweet(id: TweetId): TweetInfo = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    ) {

        val tweet = tweetDao.findById(id.value) ?: throw TweetNotFoundException(id.value)
        val user = userDao.findById(tweet.userId.value) ?: throw UserNotFoundException(tweet.id.value)
        val tags = tagDao.findTagNamesByTweetId(tweet.id.value)
        val replies = getReply(tweet.id.value)
        tweet.toInfo(user.name, tags, replies)
    }

    override fun getTimeLine(filter: TweetFilter): TimeLine = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    )
    {
        tweetDao.getTweetQuery(SqlTweetFilter(
            textFilter = filter.text?.value,
            createFilter = filter.createTime?.let {
                SqlDateTimeFilter(
                    it.from.toLocalDateTime(),
                    it.to.toLocalDateTime()
                )
            },
            tagFilter = filter.tags?.let { SqlTagFilter(it.list) }
        )).let {
            queryToTimeLine(it)
        }
    }

    fun queryToTimeLine(userQuery: Query): TimeLine = userQuery.map { resultRow ->
        val tweetId = resultRow[Tweets.id].value
        val userId = resultRow[Tweets.userId].value
        val text = resultRow[Tweets.text]
        val createdAt = resultRow[Tweets.createdAt]
        val updatedAt = resultRow[Tweets.updatedAt]
        val user = userDao.findById(userId) ?: throw UserNotFoundException(userId)
        val tags = tagDao.findTagNamesByTweetId(tweetId)
        val replies = getReply(tweetId)
        TweetInfo(
            id = tweetId.toString(),
            userName = user.name,
            text = text,
            tags = tags,
            createdAt = createdAt.atZone(ZoneId.of("UTC")).format(formatter),
            updatedAt = updatedAt.atZone(ZoneId.of("UTC")).format(formatter),
            replies = replies
        )
    }.let {
        TimeLine(it)
    }

    override fun isNotFound(tweetId: TweetId): Boolean = transaction(
        db = db,
        transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
        repetitionAttempts = 2
    ) {
        tweetDao.findById(tweetId.value) == null
    }

    fun getReply(tweetId: UUID) =
        commentDao.findByTweetId(tweetId).map {
            val replyUser = userDao.findById(it.userId.value) ?: throw UserNotFoundException(it.userId.value)
            val replyTags = tagDao.findTagNamesByCommentId(it.id.value)
            it.toReplyInfo(replyUser.name, replyTags)
        }
}

fun CommentDao.toReplyInfo(userName: String, tags: List<String>) = ReplyInfo(
    id = id.value.toString(),
    text = text,
    userName = userName,
    createdAt = createdAt.atZone(ZoneId.of("UTC")).format(formatter),
    updatedAt = updatedAt.atZone(ZoneId.of("UTC")).format(formatter),
    tags = tags
)

fun TweetDao.toInfo(userName: String, tags: List<String>, replies: List<ReplyInfo>) = TweetInfo(
    id = id.value.toString(),
    text = text,
    userName = userName,
    createdAt = createdAt.atZone(ZoneId.of("UTC")).format(formatter),
    updatedAt = updatedAt.atZone(ZoneId.of("UTC")).format(formatter),
    tags = tags,
    replies = replies
)
