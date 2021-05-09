package com.harada.usecase

import com.harada.domainmodel.user.Mail

data class AlreadyExistMailException(private val mail: Mail) : Throwable("$mail is already exist")