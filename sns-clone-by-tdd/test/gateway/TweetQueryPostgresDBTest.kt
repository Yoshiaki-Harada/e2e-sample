package gateway

import com.harada.domain.model.message.TweetId
import com.harada.domainmodel.tweet.TagFilter
import com.harada.domainmodel.tweet.TextFilter
import com.harada.domainmodel.tweet.TimeFilter
import com.harada.domainmodel.tweet.TweetFilter
import com.harada.driver.dao.*
import com.harada.formatter
import com.harada.gateway.TweetQueryPostgresDB
import com.harada.viewmodel.TimeLine
import createReplyInfo
import createTimeLine
import createTweetId
import createTweetInfo
import createUserInfo
import io.mockk.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TweetQueryPostgresDBTest {
    private val tweetDao = mockk<TweetDao.Companion>()
    private val userDao = mockk<UserDao.Companion>()
    private val commentDao = mockk<CommentDao.Companion>()
    private val tagDao = mockk<TagDao.Companion>()
    private val user1Id = UUID.fromString("11ad470d-f785-46b5-a6cf-cd7d75fbc114");
    private val mockTweet = mockk<TweetDao>() {
        every { this@mockk.id } returns EntityID(UUID.fromString(createTweetInfo().id), Tweets)
        every { this@mockk.text } returns createTweetInfo().text
        every { this@mockk.userId } returns EntityID(user1Id, Users)
        every { this@mockk.createdAt } returns LocalDateTime.parse(createTweetInfo().createdAt, formatter)
        every { this@mockk.updatedAt } returns LocalDateTime.parse(createTweetInfo().updatedAt, formatter)
    }

    private val comment = createReplyInfo(
        id = UUID.fromString("6207005e-d8ab-47ec-b483-189d7cbd726f"),
        text = "text 2",
        userName = "Tanaka Jiro",
        createdAt = ZonedDateTime.of(2020, 1, 2, 2, 0, 0, 0, ZoneId.of("UTC")),
        updatedAt = ZonedDateTime.of(2020, 1, 2, 2, 0, 0, 0, ZoneId.of("UTC"))
    )
    private val user2Id = UUID.fromString("6207005e-d8ab-47ec-b483-189d7cbd726f")
    private val mockComment = mockk<CommentDao>() {
        every { this@mockk.id } returns EntityID(UUID.fromString(comment.id), Tweets)
        every { this@mockk.text } returns comment.text
        every { this@mockk.userId } returns EntityID(user2Id, Users)
        every { this@mockk.createdAt } returns LocalDateTime.parse(comment.createdAt, formatter)
        every { this@mockk.updatedAt } returns LocalDateTime.parse(comment.updatedAt, formatter)
    }
    private val mockUser1 = mockk<UserDao>() {
        every { this@mockk.name } returns createUserInfo().name
    }

    private val mockUser2 = mockk<UserDao>() {
        every { this@mockk.name } returns comment.userName
    }

    @BeforeEach
    fun setUp() {
        clearMocks(tweetDao, userDao)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
        every {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(), db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        } answers { call ->
            lambda<Transaction.() -> Any>().invoke(mockk())
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    }

    @Test
    fun `Tweetをidを指定して取得できる`() {
        every { tweetDao.findById(any<UUID>()) } returns mockTweet
        every { userDao.findById(UUID.fromString("11ad470d-f785-46b5-a6cf-cd7d75fbc114")) } returns mockUser1
        every { userDao.findById(UUID.fromString("6207005e-d8ab-47ec-b483-189d7cbd726f")) } returns mockUser2
        every { commentDao.findByTweetId(any()) } returns listOf(mockComment)
        every { tagDao.findTagNamesByTweetId(any()) } returns listOf("tag")
        every { tagDao.findTagNamesByCommentId(any()) } returns listOf("tag")

        val query = TweetQueryPostgresDB(tweetDao, userDao, commentDao, mockk(), tagDao)
        val result = query.getTweet(TweetId(UUID.fromString(createTweetInfo().id)))

        verify {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(),
                db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        }
        verify { commentDao.findByTweetId(any()) }
        assertEquals(result, createTweetInfo(replies = listOf(comment)))
    }

    @Test
    fun `Tweetをidを指定して存在するかを確認できる`() {
        every { tweetDao.findById(any<UUID>()) } returns null
        every { userDao.findById(any<EntityID<UUID>>()) } returns mockUser1
        val query = TweetQueryPostgresDB(tweetDao, userDao, mockk(), mockk(), mockk())
        assertTrue(query.isNotFound(createTweetId()))

        verify {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(),
                db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        }
    }

    @Test
    fun `Tweetの一覧を取得できる`() {
        every { tweetDao.getTweetQuery(any()) } returns mockk()
        val query = spyk(TweetQueryPostgresDB(tweetDao, userDao, commentDao, mockk(), tagDao))
        every { query.queryToTimeLine(any()) } returns TimeLine(listOf(createTweetInfo(replies = listOf(comment))))

        val result = query.getTimeLine(TweetFilter())
        verify {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(),
                db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        }
        assertEquals(result, TimeLine(listOf(createTweetInfo(replies = listOf(comment)))))
        clearMocks(query)
    }


    @Test
    fun `Tweetをタグで検索できる`() {
        every { tweetDao.getTweetQuery(any()) } returns mockk()
        val query = spyk(TweetQueryPostgresDB(tweetDao, userDao, commentDao, mockk(), tagDao))
        every { query.queryToTimeLine(any()) } returns createTimeLine()
        query.getTimeLine(
            TweetFilter(
                tags = TagFilter(
                    listOf("tag")
                )
            )
        )
        verify {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(),
                db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        }
        verify {
            tweetDao.getTweetQuery(SqlTweetFilter(tagFilter = SqlTagFilter(listOf("tag"))))
        }
        clearMocks(query)
    }

    @Test
    fun `Tweetをテキストで検索できる`() {
        every { tweetDao.getTweetQuery(any()) } returns mockk()
        val query = spyk(TweetQueryPostgresDB(tweetDao, userDao, commentDao, mockk(), tagDao))
        every { query.queryToTimeLine(any()) } returns createTimeLine()

        query.getTimeLine(
            TweetFilter(
                text = TextFilter(
                    "test"
                )
            )
        )
        verify {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(),
                db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        }
        verify {
            tweetDao.getTweetQuery(SqlTweetFilter(textFilter = "test"))
        }
        clearMocks(query)
    }

    @Test
    fun `Tweetを作成時間で検索できる`() {
        every { tweetDao.getTweetQuery(any()) } returns mockk()
        val query = spyk(TweetQueryPostgresDB(tweetDao, userDao, commentDao, mockk(), tagDao))
        every { query.queryToTimeLine(any()) } returns createTimeLine()

        query.getTimeLine(
            TweetFilter(
                createTime = TimeFilter(
                    from = ZonedDateTime.parse(
                        "2020-06-17T10:15:30+09:00",
                        formatter
                    ), to = ZonedDateTime.parse(
                        "2020-06-18T10:15:30+09:00",
                        formatter
                    )
                )
            )
        )
        verify {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(),
                db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        }
        verify {
            tweetDao.getTweetQuery(
                SqlTweetFilter(
                    createFilter = SqlDateTimeFilter(
                        from = LocalDateTime.of(2020, 6, 17, 10, 15, 30), to = LocalDateTime.of(2020, 6, 18, 10, 15, 30)
                    )
                )
            )
        }
        clearMocks(query)
    }
}