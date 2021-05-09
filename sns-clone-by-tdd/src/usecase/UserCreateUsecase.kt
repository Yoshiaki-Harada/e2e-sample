package com.harada.usecase

import com.harada.domainmodel.user.User
import com.harada.domainmodel.user.UserId
import com.harada.port.UserQueryService
import com.harada.port.UserWriteStore

class UserCreateUseCase(
    private val userStore: UserWriteStore,
    private val query: UserQueryService
) : IUserCreateUseCase {
    override fun execute(user: User): UserId {
        if (user.mail.isInValid()) throw InvalidMailException(user.mail)
        if (query.isFoundByMail(user.mail.value)) throw AlreadyExistMailException(
            user.mail
        )
        return userStore.save(user)
    }
}

