package gateway

import com.harada.domainmodel.user.NameFilter
import com.harada.domainmodel.user.OldFilter
import com.harada.domainmodel.user.UserFilter
import com.harada.driver.dao.SqlOldFilter
import com.harada.driver.dao.SqlUserFilter
import com.harada.driver.dao.UserDao
import com.harada.formatter
import com.harada.port.UserNotFoundException
import com.harada.gateway.UserQueryPostgresDB
import com.harada.viewmodel.UsersInfo
import createUserId
import createUserInfo
import io.mockk.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.Connection
import java.sql.Date.valueOf
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class UserQueryPostgresDBTest {
    private val dao = mockk<UserDao.Companion>(relaxed = true)
    private val mockUser = mockk<UserDao>() {
        every { this@mockk.id } returns EntityID(UUID.fromString(createUserInfo().id), mockk())
        every { this@mockk.name } returns createUserInfo().name
        every { this@mockk.mail } returns createUserInfo().mail
        every { this@mockk.birthday } returns valueOf(createUserInfo().birthday).toLocalDate()
        every { this@mockk.createdAt } returns LocalDateTime.parse(createUserInfo().createdAt, formatter)
        every { this@mockk.updatedAt } returns LocalDateTime.parse(createUserInfo().updatedAt, formatter)
    }

    @BeforeEach
    fun setUp() {
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
        clearMocks(dao)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
    }

    @Test
    fun `ユーザー一覧を取得する`() {
        every { dao.get(any<SqlUserFilter>()) } returns listOf(mockUser)
        val db = UserQueryPostgresDB(dao, mockk())
        val usersInfo = db.get(UserFilter())
        verify {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(),
                db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        }
        verify { dao.get(SqlUserFilter()) }
        assertEquals(UsersInfo(listOf(createUserInfo())), usersInfo)
    }

    @Test
    fun `名前に指定した文字列が含まれるユーザー一覧を取得する`() {
        every { dao.get(any<SqlUserFilter>()) } returns listOf(mockUser)
        val query = UserQueryPostgresDB(dao, mockk())
        query.get(
            UserFilter(
                name = NameFilter(
                    "Tanaka"
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
        verify { dao.get(SqlUserFilter(nameFilter = "Tanaka")) }
    }

    @Test
    fun `年齢でフィルターしたユーザー一覧を取得する`() {
        every { mockUser.birthday } returns valueOf(createUserInfo().birthday).toLocalDate()
        every { dao.get(any<SqlUserFilter>()) } returns listOf(mockUser)

        val query = UserQueryPostgresDB(dao, mockk())
        query.get(
            UserFilter(
                old = OldFilter(
                    20,
                    30
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

        val yearMonth = LocalDate.now()
        verify { dao.get(SqlUserFilter(oldFilter = SqlOldFilter(yearMonth.minusYears(30), yearMonth.minusYears(20)))) }
    }

    @Test
    fun `指定したidのユーザを取得する`() {
        every { dao.findById(any<UUID>()) } returns mockUser
        val query = UserQueryPostgresDB(dao, mockk())
        val user = query.get(createUserId())

        verify {
            transaction(
                statement = captureLambda<Transaction.() -> Any>(),
                db = any(),
                transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED,
                repetitionAttempts = 2
            )
        }
        verify { dao.findById(createUserId().value) }
        assertEquals(user, createUserInfo())
    }

    @Test
    fun `指定したidのユーザが存在しない場合例外を投げる`() {
        every { dao.findById(any<UUID>()) } returns null
        val query = UserQueryPostgresDB(dao, mockk())
        assertThrows<UserNotFoundException> {
            query.get(createUserId())
        }
    }

    @Test
    fun `ユーザーが存在するか判定できる`() {
        every { dao.findById(any<UUID>()) } returns null
        val query = UserQueryPostgresDB(dao, mockk())
        assertTrue(query.isNotFound(createUserId()))
    }
}

