package com.harada.usecase

import com.harada.domainmodel.user.UpdateUser
import com.harada.domainmodel.user.UserId
import com.harada.port.UserNotFoundException
import com.harada.port.UserQueryService
import com.harada.port.UserWriteStore

class UserUpdateUseCase(private val store: UserWriteStore, private val query: UserQueryService) : IUserUpdateUseCase {
    override fun execute(id: UserId, user: UpdateUser) {
        if (query.isNotFound(id)) throw UserNotFoundException(id.value)
        user.mail?.let {
            if (it.isInValid()) throw InvalidMailException(it)
            if (query.isFoundByMail(user.mail.value)) throw AlreadyExistMailException(
                user.mail
            )
        }
        store.update(id, user)
    }
}
