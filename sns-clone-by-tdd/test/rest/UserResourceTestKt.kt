package rest

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.harada.domainmodel.user.NameFilter
import com.harada.domainmodel.user.OldFilter
import com.harada.domainmodel.user.UserFilter
import com.harada.domainmodel.user.UserId
import com.harada.port.UserNotFoundException
import com.harada.port.UserQueryService
import com.harada.rest.RequestUser
import com.harada.rest.userModuleWithDepth
import com.harada.usecase.IUserCreateUseCase
import com.harada.usecase.IUserUpdateUseCase
import createRequestUpdateUser
import createRequestUser
import createUpdateUser
import createUser
import createUserId
import createUserInfo
import createUsersInfo
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.server.testing.TestApplicationCall
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.singleton
import java.util.*

@KtorExperimentalLocationsAPI
class UserResourceTest {

    val gson: Gson = GsonBuilder().setPrettyPrinting().create()


    @Nested
    inner class CreateUser {

        private val createUseCase = mockk<IUserCreateUseCase>() {
            every { this@mockk.execute(any()) } returns UserId(
                UUID.fromString(
                    "A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A11"
                )
            )
        }

        private val testKodein = Kodein {
            bind<IUserCreateUseCase>() with singleton { createUseCase }
        }

        @Test
        fun `ユーザーを登録することができる`() {
            invokeWithTestUserCreateApplication(
                testKodein = testKodein,
                body = gson.toJson(RequestUser("Tanaka Taro", "test@gmail.com", "1990-01-01")),
                assert = {
                    verify { createUseCase.execute(createUser()) }
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            )
        }

        @Test
        fun `不正な日付フォーマットの誕生日のユーザーを登録するとStatus Code 400が返却される`() {
            invokeWithTestUserCreateApplication(
                testKodein = testKodein,
                body = gson.toJson(createRequestUser(birthday = "1990-")),
                assert = {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            )
        }

        @Test
        fun `不正なフォーマットのユーザーを登録するとStatus Code 400が返却される`() {
            invokeWithTestUserCreateApplication(
                testKodein = testKodein,
                body = "{\"nam\" : \"Taro\"",
                assert = {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            )
        }

        @Test
        fun `フィールドが揃っていないユーザーを登録するとStatus Code 400が返却される`() {
            invokeWithTestUserCreateApplication(
                testKodein = testKodein,
                body = "{\"name\" : \"Taro\"}",
                assert = {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            )
        }

        private fun invokeWithTestUserCreateApplication(
            testKodein: Kodein,
            body: String?,
            assert: TestApplicationCall.() -> Unit
        ) {
            invokeWithTestApplication(
                moduleFunction = {
                    userModuleWithDepth(testKodein)
                },
                path = "/users",
                method = HttpMethod.Post,
                body = body,
                contentType = ContentType.Application.Json,
                assert = assert
            )
        }
    }

    @Nested
    inner class UpdateUser {
        private val updateUseCase = mockk<IUserUpdateUseCase>() {
            every { this@mockk.execute(id = any(), user = any()) } just Runs
        }

        private val testKodein = Kodein {
            bind<IUserUpdateUseCase>() with singleton { updateUseCase }
        }

        @Test
        fun `User情報を更新できる`() {
            invokeWithTestUserUpdateApplication(
                testKodein = testKodein,
                body = gson.toJson(createRequestUpdateUser(mail = "update@gmail.com")),
                assert = {
                    assertEquals(HttpStatusCode.OK, response.status())
                    verify {
                        updateUseCase.execute(
                            id = createUserId(),
                            user = createUpdateUser(mail = "update@gmail.com")
                        )
                    }
                }
            )
        }

        @Test
        fun `不正なUserIdの形式のUser情報では更新できない`() {
            invokeWithTestUserUpdateApplication(
                testKodein = testKodein,
                userId = "wrong",
                body = gson.toJson(createRequestUpdateUser(mail = "update@gmail.com")),
                assert = {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    verify(exactly = 0) {
                        updateUseCase.execute(
                            id = any(),
                            user = any()
                        )
                    }
                }
            )
        }

        private fun invokeWithTestUserUpdateApplication(
            testKodein: Kodein,
            body: String?,
            userId: String = createUserId().value.toString(),
            assert: TestApplicationCall.() -> Unit
        ) {
            invokeWithTestApplication(
                moduleFunction = {
                    userModuleWithDepth(testKodein)
                },
                path = "/users/$userId",
                method = HttpMethod.Put,
                body = body,
                contentType = ContentType.Application.Json,
                assert = assert
            )
        }
    }

    @Nested
    inner class GetUser {
        private val query = mockk<UserQueryService>() {
            every { this@mockk.get(any<UserFilter>()) } returns createUsersInfo()
            every { this@mockk.get(any<UserId>()) } returns createUserInfo()
        }
        private val testKodein = Kodein {
            bind<UserQueryService>() with singleton { query }
        }

        @Test
        fun `ユーザー一覧を取得できる`() {
            invokeWithTestGetUsersApplication(
                testKodein = testKodein,
                assert = {
                    verify { query.get(UserFilter()) }
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(gson.toJson(createUsersInfo()), response.content)
                }
            )
        }

        @Test
        fun `名前でフィルターしたユーザー一覧を取得できる`() {
            invokeWithTestGetUsersApplication(
                testKodein = testKodein,
                path = "/users?name=Tanaka",
                assert = {
                    verify { query.get(
                        UserFilter(
                            name = NameFilter(
                                "Tanaka"
                            )
                        )
                    ) }
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            )
        }

        @Test
        fun `年齢でフィルターしたユーザー一覧を取得できる`() {
            invokeWithTestGetUsersApplication(
                testKodein = testKodein,
                path = "/users?old_from=20&old_to=25",
                assert = {
                    verify { query.get(
                        UserFilter(
                            old = OldFilter(
                                20,
                                25
                            )
                        )
                    ) }
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            )
        }

        @Test
        fun `idを指定してユーザを取得することができる`() {
            invokeWithTestGetUsersApplication(
                testKodein = testKodein,
                path = "/users/${createUserId().value}",
                assert = {
                    verify { query.get(createUserId()) }
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(gson.toJson(createUserInfo()), response.content)
                }
            )
        }

        @Test
        fun `指定したidのユーザが存在しないとき404を返す`() {

            val query = mockk<UserQueryService>() {
                every { this@mockk.get(any<UserId>()) } throws UserNotFoundException(
                    createUserId().value
                )
            }

            val testKodein = Kodein {
                bind<UserQueryService>() with singleton { query }
            }
            invokeWithTestGetUsersApplication(
                testKodein = testKodein,
                path = "/users/${createUserId().value}",
                assert = {
                    verify { query.get(createUserId()) }
                    assertEquals(HttpStatusCode.NotFound, response.status())
                }
            )
        }

        private fun invokeWithTestGetUsersApplication(
            testKodein: Kodein,
            path: String = "/users",
            body: String? = null,
            assert: TestApplicationCall.() -> Unit
        ) {
            invokeWithTestApplication(
                moduleFunction = {
                    userModuleWithDepth(testKodein)
                },
                path = path,
                method = HttpMethod.Get,
                body = body,
                contentType = ContentType.Application.Json,
                assert = assert
            )
        }
    }
}

