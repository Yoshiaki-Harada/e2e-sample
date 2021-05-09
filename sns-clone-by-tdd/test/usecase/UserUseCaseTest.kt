package usecase

import com.harada.domainmodel.user.UserId
import com.harada.port.UserNotFoundException
import com.harada.port.UserQueryService
import com.harada.port.UserWriteStore
import com.harada.usecase.AlreadyExistMailException
import com.harada.usecase.InvalidMailException
import com.harada.usecase.UserCreateUseCase
import com.harada.usecase.UserUpdateUseCase
import createUpdateUser
import createUser
import createUserId
import createUserInfo
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class UserUseCaseTest {
    val writeStore = mockk<UserWriteStore>()
    val query = mockk<UserQueryService>() {
        every { this@mockk.isFoundByMail(any()) } returns false
    }

    @BeforeEach
    fun setUp() {
        clearMocks(query)
        every { writeStore.save(any()) } returns createUserId()
        every { writeStore.update(any(), any()) } just Runs
        every { query.get(any<UserId>()) } returns createUserInfo()
        every { query.isNotFound(any<UserId>()) } returns false
        every { query.isFoundByMail(any()) } returns false
    }

    @Test
    fun `ユーザを登録することができる`() {
        val useCase = UserCreateUseCase(this.writeStore, query)
        val id = useCase.execute(createUser())
        verify { this@UserUseCaseTest.writeStore.save(createUser()) }
        assertEquals(id, createUserId())
    }

    @Test
    fun `不正なメールアドレスのユーザは登録することができない`() {
        val useCase = UserCreateUseCase(this.writeStore, query)
        assertThrows<InvalidMailException> {
            useCase.execute(createUser(mail = "wrong-address"))
        }
        verify(exactly = 0) { this@UserUseCaseTest.writeStore.save(createUser()) }
    }

    @Test
    fun `既に存在するメールアドレスではユーザを登録することができない`() {
        every { query.isFoundByMail(any()) } returns true
        val useCase = UserCreateUseCase(this.writeStore, query)
        assertThrows<AlreadyExistMailException> {
            useCase.execute(createUser(mail = "already@gamil.com"))
        }
        verify(exactly = 0) { this@UserUseCaseTest.writeStore.save(createUser()) }
    }

    @Test
    fun `ユーザを更新することができる`() {
        val useCase = UserUpdateUseCase(this.writeStore, query)
        useCase.execute(createUserId(), createUpdateUser(mail = "update@gmail.com"))
        verify { this@UserUseCaseTest.writeStore.update(createUserId(), createUpdateUser(mail = "update@gmail.com")) }
    }

    @Test
    fun `不正なメールアドレスは更新できない`() {
        val useCase = UserUpdateUseCase(this.writeStore, query)
        useCase.execute(createUserId(), createUpdateUser(mail = "update@gmail.com"))
        assertThrows<InvalidMailException> {
            useCase.execute(createUserId(), createUpdateUser(mail = "wrong-address"))
        }
        verify(exactly = 0) {
            this@UserUseCaseTest.writeStore.update(
                createUserId(),
                createUpdateUser(mail = "wrong-address")
            )
        }
    }

    @Test
    fun `既に存在するメールアドレスではユーザを更新することができない`() {
        every { query.isFoundByMail(any()) } returns true
        val useCase = UserUpdateUseCase(this.writeStore, query)
        assertThrows<AlreadyExistMailException> {
            useCase.execute(createUserId(), createUpdateUser(mail = "already@gmail.com"))
        }
        verify(exactly = 0) { this@UserUseCaseTest.writeStore.save(createUser()) }
    }

    @Test
    fun `存在しないユーザは更新できない`() {
        every { query.isNotFound(any<UserId>()) } throws UserNotFoundException(
            createUserId().value
        )
        val useCase = UserUpdateUseCase(this.writeStore, query)
        assertThrows<UserNotFoundException> {
            useCase.execute(createUserId(), createUpdateUser())
        }
    }

}