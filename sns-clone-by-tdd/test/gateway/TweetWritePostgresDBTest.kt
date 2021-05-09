package gateway

import com.harada.driver.dao.*
import com.harada.gateway.TweetWritePostgresDB
import com.harada.port.TweetNotFoundException
import createTweet
import createTweetId
import createUpdateTweet
import io.mockk.*
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class TweetWritePostgresDBTest {
    private val tweetDao = mockk<TweetDao.Companion>()
    private val commentDao = mockk<CommentDao.Companion>()
    private val tagDao = mockk<TagDao.Companion>()
    private val tagTweetMapDao = mockk<TagTweetMapDao.Companion>()
    private val tagCommentMapDao = mockk<TagCommentMapDao.Companion>()

    @BeforeEach
    fun setUp() {
        mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
        every { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) } answers { call ->
            lambda<Transaction.() -> Any>().invoke(mockk())
        }
        clearMocks(tweetDao, commentDao, tagDao, tagTweetMapDao, tagCommentMapDao)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    }

    @Test
    fun `Tweetを作成することができる`() {
        every { tweetDao.create(any()) } returns createTweetId().value
        every { tagDao.findOrCreate("tag1") } returns UUID.fromString("996561dc-a2e5-4868-963e-3ec70cd2c14f")
        every { tagDao.findOrCreate("tag2") } returns UUID.fromString("a5f39e50-e009-4ca3-92c5-e8c499ab693d")
        every { tagTweetMapDao.create(any(), any(), any()) } just Runs
        val db = TweetWritePostgresDB(tweetDao, mockk(), mockk(), tagDao, tagTweetMapDao, mockk())
        db.save(createTweet(tags = listOf("tag1", "tag2")))

        verify { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) }
        verify { tagDao.findOrCreate("tag1") }
        verify { tagTweetMapDao.create(any(), UUID.fromString("996561dc-a2e5-4868-963e-3ec70cd2c14f"), any()) }
        verify { tagDao.findOrCreate("tag2") }
        verify { tagTweetMapDao.create(any(), UUID.fromString("a5f39e50-e009-4ca3-92c5-e8c499ab693d"), any()) }
        verify { tweetDao.create(any()) }
    }

    @Test
    fun `Tweetに対してリプライできる`() {
        every { commentDao.create(any()) } returns createTweetId().value
        every { tagDao.findOrCreate(any()) } returns UUID.fromString("996561dc-a2e5-4868-963e-3ec70cd2c14f")
        every { tagCommentMapDao.create(any(), any(), any()) } just Runs
        val db = TweetWritePostgresDB(mockk(), commentDao, mockk(), tagDao, mockk(), tagCommentMapDao)
        db.save(
            createTweet(
                replyTo = UUID.fromString("7865abd1-886d-467d-ac59-7df75d010473"),
                tags = listOf("tag")
            )
        )

        verify { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) }
        verify { tagDao.findOrCreate("tag") }
        verify { tagCommentMapDao.create(any(), UUID.fromString("996561dc-a2e5-4868-963e-3ec70cd2c14f"), any()) }
        verify { commentDao.create(any()) }
    }

    @Test
    fun `Tweetを更新することができる`() {
        every { tweetDao.findById(any<UUID>()) } returns mockk()
        every { tweetDao.update(any()) } just Runs
        every { tagTweetMapDao.deleteByTweetId(any()) } just Runs
        every { tagTweetMapDao.create(any(), any(), any()) } just Runs
        every { tagDao.findOrCreate("tag") } returns UUID.fromString("996561dc-a2e5-4868-963e-3ec70cd2c14f")
        val db = TweetWritePostgresDB(tweetDao, mockk(), mockk(), tagDao, tagTweetMapDao, mockk())
        db.update(createTweetId(), createUpdateTweet())

        verify { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) }
        verify { tagTweetMapDao.deleteByTweetId(createTweetId().value) }
        verify { tagDao.findOrCreate("tag") }
        verify { tagTweetMapDao.create(any(), UUID.fromString("996561dc-a2e5-4868-963e-3ec70cd2c14f"), any()) }
        verify { tweetDao.update(any()) }
    }

    @Test
    fun `Tweet(リプライ)を更新することができる`() {
        every { tweetDao.findById(any<UUID>()) } returns null
        every { commentDao.findById(any<UUID>()) } returns mockk()
        every { commentDao.update(any()) } just Runs
        every { tagDao.findOrCreate("tag") } returns UUID.fromString("996561dc-a2e5-4868-963e-3ec70cd2c14f")
        every { tagCommentMapDao.deleteByTweetId(any()) } just Runs
        every { tagCommentMapDao.create(any(), any(), any()) } just Runs
        val db = TweetWritePostgresDB(tweetDao, commentDao, mockk(), tagDao, mockk(), tagCommentMapDao)
        db.update(createTweetId(), createUpdateTweet())

        verify { tagCommentMapDao.deleteByTweetId(createTweetId().value) }
        verify { tagDao.findOrCreate("tag") }
        verify { tagCommentMapDao.create(any(), UUID.fromString("996561dc-a2e5-4868-963e-3ec70cd2c14f"), any()) }
        verify { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) }
        verify { commentDao.update(any()) }
    }

    @Test
    fun `存在しないツイートは更新することができない`() {
        every { tweetDao.findById(any<UUID>()) } returns null
        every { commentDao.findById(any<UUID>()) } returns null
        val db = TweetWritePostgresDB(tweetDao, commentDao, mockk(), mockk(), mockk(), mockk())
        assertThrows<TweetNotFoundException> {
            db.update(createTweetId(), createUpdateTweet())
        }
    }
}