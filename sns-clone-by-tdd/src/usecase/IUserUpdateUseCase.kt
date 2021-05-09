package com.harada.usecase

import com.harada.domainmodel.user.UpdateUser
import com.harada.domainmodel.user.UserId

interface IUserUpdateUseCase {
    fun execute(id: UserId, user : UpdateUser)
}
