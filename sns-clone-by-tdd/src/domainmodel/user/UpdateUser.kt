package com.harada.domainmodel.user

import java.util.*

data class UpdateUser(
    val name: UserName?,
    val mail: Mail?,
    val birthday: Date?
)
