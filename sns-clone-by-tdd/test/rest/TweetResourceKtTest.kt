package rest

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.harada.domain.model.message.TweetId
import com.harada.domainmodel.tweet.TagFilter
import com.harada.domainmodel.tweet.TextFilter
import com.harada.domainmodel.tweet.TimeFilter
import com.harada.domainmodel.tweet.TweetFilter
import com.harada.formatter
import com.harada.getUUID
import com.harada.port.TweetQueryService
import com.harada.rest.ResponseTweetId
import com.harada.rest.tweetModuleWithDepth
import com.harada.usecase.ITweetCreateUseCase
import com.harada.usecase.ITweetUpdateUseCase
import createRequestTweet
import createRequestUpdateTweet
import createTweet
import createTweetId
import createTweetInfo
import createTimeLine
import createUpdateTweet
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.mockk.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals

@KtorExperimentalLocationsAPI
class TweetResourceKtTest {
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    @Nested
    inner class CreateTweet {
        private val useCase = mockk<ITweetCreateUseCase>() {
            every { this@mockk.execute(any()) } returns createTweetId()
        }
        private val testKodein = Kodein {
            bind<ITweetCreateUseCase>() with singleton { useCase }
        }

        @Test
        fun `Tweetを作成する`() {
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "/tweets",
                method = HttpMethod.Post,
                body = gson.toJson(createRequestTweet()),
                contentType = ContentType.Application.Json,
                assert = {
                    verify { useCase.execute(createTweet()) }
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(gson.toJson(ResponseTweetId(createTweetId().value.toString())), response.content)
                }
            )
        }

        @Test
        fun `Tweetに対してリプライできる`() {
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "/tweets",
                method = HttpMethod.Post,
                body = gson.toJson(createRequestTweet(replyTo = "7865abd1-886d-467d-ac59-7df75d010473")),
                contentType = ContentType.Application.Json,
                assert = {
                    verify { useCase.execute(createTweet(replyTo = getUUID("7865abd1-886d-467d-ac59-7df75d010473"))) }
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            )
        }

        @Test
        fun `不正なフォーマットのツイートを作成しようとするとStatusCode 400が返却される`() {
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "/tweets",
                method = HttpMethod.Post,
                body = "{\"tex\": \"test\"}",
                contentType = ContentType.Application.Json,
                assert = {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            )
        }
    }

    @Nested
    inner class UpdateTweet {
        private val useCase = mockk<ITweetUpdateUseCase>() {
            every { this@mockk.execute(any(), any()) } just Runs
        }
        private val testKodein = Kodein {
            bind<ITweetUpdateUseCase>() with singleton { useCase }
        }

        @Test
        fun `メッセージを更新する`() {
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "/tweets/${createTweetId().value}",
                method = HttpMethod.Put,
                body = gson.toJson(createRequestUpdateTweet()),
                contentType = ContentType.Application.Json,
                assert = {
                    verify { useCase.execute(createTweetId(), createUpdateTweet()) }
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            )
        }
    }

    @Nested
    inner class GetTweets {
        private val query = mockk<TweetQueryService>() {
            every { this@mockk.getTimeLine(any<TweetFilter>()) } returns createTimeLine()
            every { this@mockk.getTweet(any<TweetId>()) } returns createTweetInfo()
        }
        private val testKodein = Kodein {
            bind<TweetQueryService>() with singleton { query }
        }

        @Test
        fun `idを指定してTweetを取得できる`() {
            val id = createTweetInfo().id
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "/tweets/$id",
                method = HttpMethod.Get,
                body = gson.toJson(createRequestUpdateTweet()),
                contentType = ContentType.Application.Json,
                assert = {
                    verify { query.getTweet(TweetId(UUID.fromString(id))) }
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            )
        }

        @Test
        fun `ツイートの一覧を取得できる`() {
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "/tweets",
                method = HttpMethod.Get,
                body = gson.toJson(createRequestUpdateTweet()),
                contentType = ContentType.Application.Json,
                assert = {
                    verify { query.getTimeLine(TweetFilter()) }
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            )
        }

        @Test
        fun `Tweetをタグで検索できる`() {
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "/tweets?tags=a,b",
                method = HttpMethod.Get,
                body = gson.toJson(createRequestUpdateTweet()),
                contentType = ContentType.Application.Json,
                assert = {
                    verify {
                        query.getTimeLine(
                            TweetFilter(
                                tags = TagFilter(listOf("a", "b"))
                            )
                        )
                    }
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            )
        }

        @Test
        fun `Tweetをテキストで検索できる`() {
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "/tweets?text=test",
                method = HttpMethod.Get,
                body = gson.toJson(createRequestUpdateTweet()),
                contentType = ContentType.Application.Json,
                assert = {
                    verify {
                        query.getTimeLine(
                            TweetFilter(
                                TextFilter(
                                    "test"
                                )
                            )
                        )
                    }
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            )
        }

        @Test
        fun `Tweetを作成時刻でフィルターできる`() {
            invokeWithTestApplication(
                moduleFunction = {
                    tweetModuleWithDepth(testKodein)
                },
                path = "/tweets?createdFrom=2020-06-15T10:15:30%2B09:00&createdTo=2020-06-17T10:15:30%2B09:00",
                method = HttpMethod.Get,
                body = gson.toJson(createRequestUpdateTweet()),
                contentType = ContentType.Application.Json,
                assert = {
                    verify {
                        query.getTimeLine(
                            TweetFilter(
                                createTime = TimeFilter(
                                    ZonedDateTime.parse("2020-06-15T10:15:30+09:00", formatter),
                                    ZonedDateTime.parse("2020-06-17T10:15:30+09:00", formatter)
                                )
                            )
                        )
                    }
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            )
        }
    }
}