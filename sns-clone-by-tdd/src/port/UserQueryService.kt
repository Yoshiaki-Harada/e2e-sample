package com.harada.port

import com.harada.domainmodel.user.UserFilter
import com.harada.domainmodel.user.UserId
import com.harada.viewmodel.UserInfo
import com.harada.viewmodel.UsersInfo

interface UserQueryService {
    fun get(filter: UserFilter): UsersInfo
    fun get(id: UserId): UserInfo
    fun isFoundByMail(mail: String): Boolean
    fun isNotFound(id: UserId): Boolean
}
