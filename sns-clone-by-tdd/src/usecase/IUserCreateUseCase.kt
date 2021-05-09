package com.harada.usecase

import com.harada.domainmodel.user.User
import com.harada.domainmodel.user.UserId

interface IUserCreateUseCase {
    fun execute(user: User): UserId
}