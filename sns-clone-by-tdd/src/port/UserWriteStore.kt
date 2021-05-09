package com.harada.port

import com.harada.domainmodel.user.UpdateUser
import com.harada.domainmodel.user.User
import com.harada.domainmodel.user.UserId

interface UserWriteStore {
    fun save(user: User): UserId
    fun update(userId: UserId, user: UpdateUser)
}