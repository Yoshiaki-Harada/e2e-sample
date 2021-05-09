package gateway

import com.harada.driver.dao.UserDao
import com.harada.gateway.UserWritePostgresDB
import com.harada.port.TweetWriteStore
import com.harada.usecase.TweetCreateUseCase
import createUpdateTweet
import createUpdateUser
import createUser
import createUserId
import io.mockk.*
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class UserWritePostgresDBTest {
    val dao = mockk<UserDao.Companion>(relaxed = true) {
        every { this@mockk.create(any()) } returns createUserId().value
    }

    @BeforeEach
    fun setUp() {
        mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
        every { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) } answers { call ->
            lambda<Transaction.() -> Any>().invoke(mockk())
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    }

    @Test
    fun `ユーザー情報をDBに保存する`() {
        val db = UserWritePostgresDB(dao, mockk())
        db.save(createUser())
        verify { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) }
        verify { dao.create(any()) }
    }

    @Test
    fun `databaseのユーザー情報を更新する`() {
        every { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) } answers { call ->
            lambda<Transaction.() -> Any>().invoke(mockk())
        }
        val db = UserWritePostgresDB(dao, mockk())
        db.update(createUserId(), createUpdateUser(mail = "test@gmail.com"))
        verify { transaction(statement = captureLambda<Transaction.() -> Any>(), db = any()) }
        verify { dao.update(any()) }
    }
}

