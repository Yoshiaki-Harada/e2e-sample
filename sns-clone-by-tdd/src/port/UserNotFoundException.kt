package com.harada.port

import java.util.*

data class UserNotFoundException(val userId:UUID) : Throwable("user: $userId not found")
