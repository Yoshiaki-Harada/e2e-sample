package usecase

import com.harada.domain.model.message.LIMIT_TWEET_LENGTH
import com.harada.port.TweetNotFoundException
import com.harada.port.UserNotFoundException
import com.harada.port.TweetQueryService
import com.harada.port.TweetWriteStore
import com.harada.port.UserQueryService
import com.harada.usecase.OverTweetLengthException
import com.harada.usecase.TweetCreateUseCase
import com.harada.usecase.TweetUpdateUseCase
import createTweet
import createTweetId
import createUpdateTweet
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals

class TweetUseCaseTest {
    private val userQuery = mockk<UserQueryService>()
    private val store = mockk<TweetWriteStore>()
    private val tweetQuery = mockk<TweetQueryService>()

    @BeforeEach
    fun setUp() {
        clearMocks(userQuery, store)
    }

    @Test
    fun `Tweetを作成することができる`() {
        val tweet = createTweet()
        every { userQuery.isNotFound(tweet.userId) } returns false
        val store = mockk<TweetWriteStore>()
        every { store.save(any()) } returns createTweetId()
        val useCase = TweetCreateUseCase(store, userQuery, mockk())
        val tweetId = useCase.execute(tweet)

        verify { store.save(tweet) }
        assertEquals(tweetId, createTweetId())
    }

    @Test
    fun `存在しないTweetに対してはリプライできない`() {
        val tweet = createTweet(replyTo = UUID.fromString("7865abd1-886d-467d-ac59-7df75d010473"))
        every { userQuery.isNotFound(tweet.userId) } returns false
        every { tweetQuery.isNotFound(any()) } returns true
        val store = mockk<TweetWriteStore>()
        every { store.save(any()) } returns createTweetId()
        val useCase = TweetCreateUseCase(store, userQuery, tweetQuery)

        assertThrows<TweetNotFoundException> {
            useCase.execute(tweet)
        }
    }

    @Test
    fun `文字数制限を超えたTweetを作成することができない`() {
        val tweet = createTweet(text = "1".repeat(LIMIT_TWEET_LENGTH + 1))
        every { userQuery.isNotFound(tweet.userId) } returns false
        every { store.save(any()) } returns createTweetId()
        val useCase = TweetCreateUseCase(store, userQuery, mockk())
        assertThrows<OverTweetLengthException> {
            useCase.execute(tweet)
        }
        verify(exactly = 0) { store.save(tweet) }
    }

    @Test
    fun `存在しないユーザではTweetを作成することができない`() {
        val tweet = createTweet(text = "1".repeat(LIMIT_TWEET_LENGTH + 1))
        every { userQuery.isNotFound(tweet.userId) } returns true
        every { store.save(any()) } returns createTweetId()
        val useCase = TweetCreateUseCase(store, userQuery, mockk())
        assertThrows<UserNotFoundException> {
            useCase.execute(tweet)
        }
        verify(exactly = 0) { store.save(tweet) }
    }

    @Test
    fun `Tweetを更新することができる`() {
        every { store.update(any(), any()) } just Runs
        val useCase = TweetUpdateUseCase(store)
        useCase.execute(createTweetId(), createUpdateTweet())
        verify { store.update(createTweetId(), createUpdateTweet()) }
    }

    @Test
    fun `文字数制限を超えるTweetは更新できない`() {
        every { store.update(any(), any()) } just Runs
        val useCase = TweetUpdateUseCase(store)
        assertThrows<OverTweetLengthException> {
            useCase.execute(createTweetId(), createUpdateTweet(text = "1".repeat(LIMIT_TWEET_LENGTH + 1)))
        }
        verify(exactly = 0) { store.update(createTweetId(), createUpdateTweet()) }
    }

}